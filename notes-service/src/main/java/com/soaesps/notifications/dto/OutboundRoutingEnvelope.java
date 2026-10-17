package com.soaesps.notifications.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unified transactional envelope carrying compiled text title, body, and a list of target routing destinations.
 * Optimizes network throughput by batching multiple endpoints (e.g., several emails) into a single pipeline payload.
 */
public record OutboundRoutingEnvelope(
        UUID userId,
        String channelType,                    // EMAIL, TELEGRAM, SMS, PUSH
        List<String> destinations,             // Collection of target addresses (e.g., [email1, email2])
        List<Map<String, String>> routingMeta, // Metadata array matching the destinations index size
        String messageTitle,                   // Fully compiled Thymeleaf layout title/subject
        String messageBody                     // Fully compiled Thymeleaf layout text body
) {}