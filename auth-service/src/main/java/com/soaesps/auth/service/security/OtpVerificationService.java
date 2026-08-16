package com.soaesps.auth.service.security;

public interface OtpVerificationService {
    /**
     * Validates the one-time password code against the user session or secret key.
     *
     * @param username the target username attempting login
     * @param code     the submitted numeric string code
     * @return true if validation passes successfully, false otherwise
     */
    boolean validateCode(String username, String code);
}
