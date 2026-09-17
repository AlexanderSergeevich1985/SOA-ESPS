package com.soaesps.aggregator.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * Outbound contract for the [user-advice] topic, consumed by notifications-service.
 *
 * @param type      advice kind: "anomaly" (realtime) or "summary" (periodic report)
 * @param userId    owner of the device; used as the Kafka partition key
 * @param deviceId  affected device; null for periodic summaries
 * @param severity  "high" | "medium" for anomalies; null for summaries
 * @param message   human-readable advice text
 * @param timestamp creation time (UTC)
 */
public record UserAdviceEvent(
        String type,
        String triggerKind,
        long userId,
        String deviceId,
        String severity,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) {
    public static final String TYPE_ANOMALY = "anomaly";
    public static final String TYPE_SUMMARY = "summary";

    public static final String TRIGGER_SCHEDULED = "scheduled";
    public static final String TRIGGER_ON_DEMAND = "on-demand";
    public static final String TRIGGER_STREAMING = "streaming";
}