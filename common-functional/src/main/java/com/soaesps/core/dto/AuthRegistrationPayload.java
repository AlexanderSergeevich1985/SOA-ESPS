package com.soaesps.core.dto;

import java.util.UUID;

/**
 * Enhanced authentication payload containing credential profiles, MFA status,
 * and primary communication delivery targets for multi-factor validation routing.
 *
 * @param outboxMessageId unique tracking identifier generated automatically for deduplication
 */
public record AuthRegistrationPayload(
        String outboxMessageId,
        String username,
        String password,
        boolean isMfaEnabled,
        String email,
        String telephone
) {
    /**
     * Compact constructor enforcing automatic id generation if it is null
     * (e.g., during creation or fallback scenarios).
     */
    public AuthRegistrationPayload {
        if (outboxMessageId == null) {
            outboxMessageId = UUID.randomUUID().toString();
        }
    }

    /**
     * Overloaded secondary constructor for clean object instantiation in business logic.
     * Automatically passes null for outboxMessageId to trigger the compact constructor's generator.
     */
    public AuthRegistrationPayload(String username, String password, boolean isMfaEnabled,
                                   String email, String telephone) {
        this(null, username, password, isMfaEnabled, email, telephone);
    }
}