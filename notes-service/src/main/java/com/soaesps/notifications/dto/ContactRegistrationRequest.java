package com.soaesps.notifications.dto;

import java.util.Map;

/**
 * Unified inbound payload model for registration and modification actions.
 */
public record ContactRegistrationRequest(
        String type,               // "SMS", "EMAIL", "TELEGRAM", "PUSH"
        boolean primary,
        Map<String, String> meta// Bags: {"phoneNumber":"..."}, {"pushToken":"...", "deviceId":"...", "deviceType":"..."}
) {}