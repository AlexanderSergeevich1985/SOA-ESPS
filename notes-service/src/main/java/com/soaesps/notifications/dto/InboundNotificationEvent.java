package com.soaesps.notifications.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.util.Map;
import java.util.UUID;

/**
 * Contract for incoming Kafka notification payloads.
 * Guarantees identity tracking and notification categorization.
 */
public record InboundNotificationEvent(
        @Id
        String notificationId,

        @Column("user_id")
        UUID userId,

        String notificationType, // e.g., "PAYMENT_SUCCESS", "SECURITY_ALERT"

        Map<String, Object> parameters // Dynamic data used to populate message templates
) {}
