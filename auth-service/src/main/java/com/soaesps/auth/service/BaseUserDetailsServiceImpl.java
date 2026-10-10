package com.soaesps.auth.service;

import com.soaesps.auth.repository.UserDetailsRepository;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.DataModels.security.Role;
import com.soaesps.core.Utils.DateTimeHelper;
import com.soaesps.core.dto.AuthRegistrationPayload;
import com.soaesps.core.exception.ExceptionMsg;
import com.soaesps.core.exception.UserAlreadyExistAuthException;
import com.soaesps.core.security.checker.BaseUserDetailsChecker;
import com.soaesps.core.security.util.SecurityHelper;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation managing standard and enterprise user credentials lifecycle.
 * Fully transaction-safe to handle concurrent processing from both HTTP REST API and async Kafka event loops.
 */
@Service("baseUserDetailsServiceImpl")
@Transactional // Declares all service capabilities operational inside individual transaction workspaces by default
public class BaseUserDetailsServiceImpl implements BaseUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(BaseUserDetailsServiceImpl.class);

    private final BaseUserDetailsChecker baseUserDetailsChecker;
    private final UserDetailsRepository repository;

    public BaseUserDetailsServiceImpl(BaseUserDetailsChecker baseUserDetailsChecker, UserDetailsRepository repository) {
        this.baseUserDetailsChecker = baseUserDetailsChecker;
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true) // Optimization for pure extraction query pipeline execution
    public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException {
        final Optional<BaseUserDetails> result = this.repository.findByUsername(userName);
        if(result.isEmpty()) {
            throw new UsernameNotFoundException(userName);
        }
        return result.get();
    }

    @Override
    public Long createUserAccount(final BaseUserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User details payload cannot be null");
        }
        final Optional<BaseUserDetails> result = this.repository.findByUsername(userDetails.getUsername());
        Assert.isTrue(result.isEmpty(), "[BaseUserDetailsServiceImpl/createUserAccount]: profile already exists: " + userDetails.getUsername());
        BaseUserDetails bud = this.repository.save(userDetails);
        return bud.getId();
    }

    @Override
    @Transactional
    public Long createUserAccount(final AuthRegistrationPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("AuthRegistrationPayload event mapping data cannot be null");
        }

        // Check if user already exists to protect data layer integrity
        final Optional<BaseUserDetails> existingUser = this.repository.findByUsername(payload.username());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistAuthException(
                    "Security execution halted: profile already exists for username: " + payload.username()
            );
        }

        // Instantiate and map fields from incoming DTO payload record to DB Entity model
        BaseUserDetails userDetailsEntity = new BaseUserDetails();
        userDetailsEntity.setUsername(payload.username());

        userDetailsEntity.setPassword(payload.password());

        // Map baseline configuration metadata for active new identities
        userDetailsEntity.setEnabled(true);
        userDetailsEntity.setAccountNonLocked(true);
        userDetailsEntity.setAccountNonExpired(true);
        userDetailsEntity.setCredentialsNonExpired(true);

        // Persist mapped security domain model inside the database
        BaseUserDetails savedEntity = this.repository.save(userDetailsEntity);

        logger.info("Successfully persisted brand new user security record via Kafka lifecycle stream. DB ID: {}", savedEntity.getId());
        return savedEntity.getId();
    }


    @Override
    public boolean updateUserAccount(final String name, final BaseUserDetails userDetails) {
        if(name == null || name.isEmpty() || userDetails == null) {
            return false;
        }
        final Optional<BaseUserDetails> result = this.repository.findByUsername(name);
        Assert.isTrue(result.isPresent(), "[BaseUserDetailsServiceImpl/updateUserAccount]: profile doesn't exist: " + name);
        final BaseUserDetails existing = result.get();

        existing.setUsername(userDetails.getUsername());
        existing.setPassword(userDetails.getPassword());
        existing.setAuthorities(userDetails.getAuthorities());

        this.repository.save(existing);
        return true;
    }

    @Override
    public boolean deleteUserAccount(final String name) {
        if(name == null || name.isEmpty()) {
            return false;
        }
        final Optional<BaseUserDetails> result = this.repository.findByUsername(name);
        Assert.isTrue(result.isPresent(), "[BaseUserDetailsServiceImpl/deleteUserAccount]: profile doesn't exist: " + name);
        final BaseUserDetails existing = result.get();

        this.repository.delete(existing);
        return true;
    }

    @Override
    public void createUser(UserDetails user) {
        baseUserDetailsChecker.check(user);
        Optional<BaseUserDetails> result = repository.findByUsername(user.getUsername());
        if (result.isPresent()) {
            throw new UserAlreadyExistAuthException(ExceptionMsg.getUserAlreadyExistMsg(user.getUsername()));
        }
        repository.save(userDetailsToBaseUserDetails(user));
    }

    @Override
    public void updateUser(UserDetails user) {
        baseUserDetailsChecker.check(user);
        Optional<BaseUserDetails> result = repository.findByUsername(user.getUsername());
        if (result.isEmpty()) {
            throw new UsernameNotFoundException(ExceptionMsg.getUserNotFoundMsg(user.getUsername()));
        }
        BaseUserDetails userDetails = result.get();
        userDetails.setUsername(user.getUsername());
        userDetails.setPassword(user.getPassword());
        userDetails.setAuthorities(user.getAuthorities().stream()
                .map(a -> (Role) a)
                .collect(Collectors.toList()));
        userDetails.setAccountNonExpired(user.isAccountNonExpired());
        userDetails.setAccountNonLocked(user.isAccountNonLocked());
        userDetails.setCredentialsNonExpired(user.isCredentialsNonExpired());
        userDetails.setEnabled(user.isEnabled());
        repository.save(userDetails);
    }

    @Override
    public void deleteUser(String username) {
        if(username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username placeholder payload signature required");
        }
        Optional<BaseUserDetails> userDetails = repository.findByUsername(username);
        if (userDetails.isEmpty()) {
            throw new UsernameNotFoundException(ExceptionMsg.getUserNotFoundMsg(username));
        }
        long userId = userDetails.get().getId();
        repository.deleteById(userId);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        BaseUserDetails userDetails = loadCurrentUser();
        userDetails.setPassword(newPassword);
        repository.save(userDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        Optional<BaseUserDetails> userDetails = repository.findByUsername(username);
        return userDetails.isPresent();
    }

    /**
     * Resolves currently active caller session identity.
     * Enforces Propagation.REQUIRED to cleanly integrate inside active context tasks
     * or instantiate a new dedicated transaction database connection if invoked standalone.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseUserDetails loadCurrentUser() {
        String username = SecurityHelper.getCurrentLogin();
        Optional<BaseUserDetails> userDetails = repository.findByUsername(username);
        if (userDetails.isEmpty()) {
            throw new UsernameNotFoundException(ExceptionMsg.getUserNotFoundMsg(username));
        }
        return userDetails.get();
    }

    private static BaseUserDetails userDetailsToBaseUserDetails(UserDetails user) {
        if (user instanceof BaseUserDetails) {
            return (BaseUserDetails) user;
        }
        BaseUserDetails bud = new BaseUserDetails();
        bud.setUsername(user.getUsername());
        bud.setPassword(user.getPassword());
        bud.setCreationTime(DateTimeHelper.getCurrentTimeWithTimeZone("UTC"));
        bud.setModificationTime(bud.getCreationTime());
        bud.setEnabled(user.isEnabled());
        bud.setAccountNonLocked(user.isAccountNonLocked());
        bud.setAccountNonExpired(user.isAccountNonExpired());
        bud.setCredentialsNonExpired(user.isCredentialsNonExpired());

        return bud;
    }
}