package com.soaesps.auth.service;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.dto.AuthRegistrationPayload;

import org.springframework.security.provisioning.UserDetailsManager;

public interface BaseUserDetailsService extends UserDetailsManager {
    Long createUserAccount(final BaseUserDetails userDetails);

    Long createUserAccount(AuthRegistrationPayload payload);

    boolean updateUserAccount(final String name, final BaseUserDetails userDetails);

    boolean deleteUserAccount(final String name);
}