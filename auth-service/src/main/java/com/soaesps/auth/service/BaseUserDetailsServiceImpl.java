package com.soaesps.auth.service;

import com.soaesps.auth.repository.UserDetailsRepository;
import com.soaesps.core.DataModels.security.BaseUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Service("baseUserDetailsServiceImpl")
public class BaseUserDetailsServiceImpl implements BaseUserDetailsService {
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

    public void createUser(BaseUserDetails user) {
        repository.save(user);
    }

    public void createUser(UserDetails user) {
        //repository.save(user);
    }

    /**
     * Update the specified user.
     */
    public void updateUser(UserDetails user) {
        //repository.save(user);
    }

    /**
     * Remove the user with the given login name from the system.
     */
    public void deleteUser(String username) {}

    /**
     * Modify the current user's password. This should change the user's password in the
     * persistent user repository (datbase, LDAP etc).
     *
     * @param oldPassword current password (for re-authentication if required)
     * @param newPassword the password to change to
     */
    public void changePassword(String oldPassword, String newPassword) {}

    /**
     * Check if a user with the supplied login name exists in the system.
     */
    public boolean userExists(String username) {
        return true;
    }
}