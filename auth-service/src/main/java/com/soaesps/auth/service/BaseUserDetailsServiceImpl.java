package com.soaesps.auth.service;

import com.soaesps.auth.repository.UserDetailsRepository;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
public class BaseUserDetailsServiceImpl implements BaseUserDetailsService {
    @Autowired
    private UserDetailsRepository repository;

    public UserDetails loadUserByUsername(final String userName) {
        Optional<BaseUserDetails> result = this.repository.findByName(userName);
        if(!result.isPresent()) return null;
        return result.get();
    }

    public boolean createUserAccount(final BaseUserDetails userDetails) {
        if(userDetails == null) return false;
        Optional<BaseUserDetails> result = this.repository.findByName(userDetails.getUsername());
        Assert.isTrue(!result.isPresent(), "profile already exists: " + userDetails.getUsername());
        this.repository.save(userDetails);
        return true;
    }

    public boolean updateUserAccount(final String name, final BaseUserDetails userDetails) {
        if(name == null || name.isEmpty() || userDetails == null) return false;
        Optional<BaseUserDetails> result = this.repository.findByName(name);
        Assert.isTrue(result.isPresent(), "profile doesn't exists: " + userDetails.getUsername());
        BaseUserDetails existing = result.get();

        existing.setUserName(userDetails.getUsername());
        existing.setPassword(userDetails.getPassword());
        existing.setAuthorities(userDetails.getAuthorities());

        this.repository.save(existing);
        return true;
    }

    public boolean deleteUserAccount(final String name) {
        if(name == null || name.isEmpty()) return false;
        Optional<BaseUserDetails> result = this.repository.findByName(name);
        Assert.isTrue(result.isPresent(), "profile doesn't exists: " + name);
        BaseUserDetails existing = result.get();

        this.repository.delete(existing);
        return true;
    }
}