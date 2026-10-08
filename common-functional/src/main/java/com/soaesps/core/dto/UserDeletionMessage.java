package com.soaesps.core.dto;

import java.io.Serializable;

/**
 * Shared infrastructure data transfer object representing a global user deletion event.
 * Standardizes account revocation commands across all SOA functional domains.
 */
public record UserDeletionMessage(
        String username,
        String action // Always set to "DELETE"
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method to build a compliant deletion event contract.
     */
    public static UserDeletionMessage forUser(String username) {
        return new UserDeletionMessage(username, "DELETE");
    }
}
