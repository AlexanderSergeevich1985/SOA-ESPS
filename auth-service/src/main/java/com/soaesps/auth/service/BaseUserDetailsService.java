package com.soaesps.auth.service;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.security.provisioning.UserDetailsManager;

public interface BaseUserDetailsService extends UserDetailsManager {
    boolean createUserAccount(final BaseUserDetails userDetails);

    boolean updateUserAccount(final String name, final BaseUserDetails userDetails);

    boolean deleteUserAccount(final String name);
}