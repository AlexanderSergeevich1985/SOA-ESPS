package com.soaesps.core.dto;

/**
 * Enhanced authentication payload containing credential profiles, MFA status,
 * and primary communication delivery targets for multi-factor validation routing.
 */
public record AuthRegistrationPayload(
        String username,
        String password,
        boolean isMfaEnabled,
        String email,        // Sourced from UserInfo for MFA OTP email delivery
        String telephone     // Sourced from UserInfo for MFA OTP SMS delivery
) {}
