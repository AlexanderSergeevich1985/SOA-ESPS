package com.soaesps.coordinator.domain;

/**
 * Record representing the acknowledgment of message delivery.
 * Used for client-side exactly-once semantics verification.
 */
public record DeliveryReceipt(
        String messageId,
        int replicaCount,
        DeliveryStatus status,
        long iterationMarker
) {
    public enum DeliveryStatus {
        DELIVERED, FAILED, PENDING
    }
}