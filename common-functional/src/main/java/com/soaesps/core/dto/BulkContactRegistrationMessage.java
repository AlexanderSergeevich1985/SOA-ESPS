package com.soaesps.core.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unified bulk data transfer object encapsulating multi-channel contact routing structures.
 * Designed to seamlessly integrate with MsgEntityConverter factories across SOA topologies.
 */
public record BulkContactRegistrationMessage(
        String username,
        List<ContactRegistrationRequest> contacts
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Failsafe validation utility to ensure the container isn't dispatched with corrupted state bounds.
     */
    public boolean isValidContext() {
        return username != null && !username.isBlank() && contacts != null;
    }

    /**
     * Factory utility to build a unified messaging contract from baseline registration attributes.
     */
    public static BulkContactRegistrationMessage fromEmailAndPhone(String username, String email, String telephone) {
        List<ContactRegistrationRequest> contactList = new ArrayList<>();

        // Pack the baseline email configuration if provided
        if (email != null && !email.isBlank()) {
            contactList.add(new ContactRegistrationRequest(
                    "EMAIL",
                    true, // Set as primary contact method by default during registration phase
                    Map.of("emailAddress", email)
            ));
        }

        // Pack the baseline phone configuration if provided
        if (telephone != null && !telephone.isBlank()) {
            contactList.add(new ContactRegistrationRequest(
                    "SMS",
                    true, // Set as primary contact method by default during registration phase
                    Map.of("phoneNumber", telephone)
            ));
        }

        return new BulkContactRegistrationMessage(username, contactList);
    }
}
