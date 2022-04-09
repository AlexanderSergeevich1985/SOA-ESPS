package com.soaesps.auth.service;

import com.soaesps.auth.repository.UserDetailsRepository;
import com.soaesps.core.DataModels.security.BaseUserDetails;

import com.soaesps.core.DataModels.security.Role;
import com.soaesps.core.Utils.DateTimeHelper;
import com.soaesps.core.exception.ExceptionMsg;
import com.soaesps.core.exception.UserAlreadyExistAuthException;
import com.soaesps.core.security.checker.BaseUserDetailsChecker;
import com.soaesps.core.security.util.SecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.stream.Collectors;

@Service("baseUserDetailsServiceImpl")
public class BaseUserDetailsServiceImpl implements BaseUserDetailsService {
    @Autowired
    private BaseUserDetailsChecker baseUserDetailsChecker;

    @Autowired
    private UserDetailsRepository repository;

    @Override
    public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException {
        final Optional<BaseUserDetails> result = this.repository.findByUserName(userName);
        if(result == null || !result.isPresent()) {
            throw new UsernameNotFoundException(userName);
        }

        return result.get();
    }

    @Override
    public boolean createUserAccount(final BaseUserDetails userDetails) {
        if(userDetails == null) {
            return false;
        }
        final Optional<BaseUserDetails> result = this.repository.findByUserName(userDetails.getUsername());
        Assert.isTrue(!result.isPresent(), "[BaseUserDetailsServiceImpl/createUserAccount]: profile already exists: " + userDetails.getUsername());
        this.repository.save(userDetails);

        return true;
    }

    @Override
    public boolean updateUserAccount(final String name, final BaseUserDetails userDetails) {
        if(name == null || name.isEmpty() || userDetails == null) {
            return false;
        }
        final Optional<BaseUserDetails> result = this.repository.findByUserName(name);
        Assert.isTrue(result.isPresent(), "[BaseUserDetailsServiceImpl/updateUserAccount]: profile doesn't exists: " + userDetails.getUsername());
        final BaseUserDetails existing = result.get();

        existing.setUserName(userDetails.getUsername());
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
        final Optional<BaseUserDetails> result = this.repository.findByUserName(name);
        Assert.isTrue(result.isPresent(), "[BaseUserDetailsServiceImpl/deleteUserAccount]: profile doesn't exists: " + name);
        final BaseUserDetails existing = result.get();

        this.repository.delete(existing);

        return true;
    }

    @Override
    public void createUser(UserDetails user) {
        baseUserDetailsChecker.check(user);
        Optional<BaseUserDetails> result = repository.findByUserName(user.getUsername());
        if (result.isPresent()) {
            throw new UserAlreadyExistAuthException(ExceptionMsg.getUserAlreadyExistMsg(user.getUsername()));
        }
        repository.save(userDetailsToBaseUserDetails(user));
    }

    @Override
    public void updateUser(UserDetails user) {
        baseUserDetailsChecker.check(user);
        Optional<BaseUserDetails> result = repository.findByUserName(user.getUsername());
        if (!result.isPresent()) {
            throw new UsernameNotFoundException(ExceptionMsg.getUserNotFoundMsg(user.getUsername()));
        }
        BaseUserDetails userDetails = result.get();
        userDetails.setUserName(user.getUsername());
        userDetails.setPassword(user.getUsername());
        userDetails.setAuthorities(user.getAuthorities().stream().map(a -> (Role) a)
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
            throw new IllegalArgumentException();
        }
        Optional<BaseUserDetails> userDetails = repository.findByUserName(username);
        if (!userDetails.isPresent()) {
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
    public boolean userExists(String username) {
        Optional<BaseUserDetails> userDetails = repository.findByUserName(username);

        return userDetails.isPresent();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public BaseUserDetails loadCurrentUser() {
        String username = SecurityHelper.getCurrentLogin();
        Optional<BaseUserDetails> userDetails = repository.findByUserName(username);
        if (!userDetails.isPresent()) {
            throw new UsernameNotFoundException(ExceptionMsg.getUserNotFoundMsg(username));
        }

        return userDetails.get();
    }

    private static BaseUserDetails userDetailsToBaseUserDetails(UserDetails user) {
        if (user instanceof BaseUserDetails) {
            return (BaseUserDetails) user;
        }
        BaseUserDetails bud = new BaseUserDetails();
        bud.setUserName(user.getUsername());
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