package com.soaesps.core.dto;

/**
 * Defines the processing states of an outbox message.
 */
public enum MessageStatus {
    /**
     * The message is registered and waiting to be processed by the relay.
     */
    PENDING,

    /**
     * Successfully published to the message broker. Can be archived or deleted.
     */
    COMPLETED,

    /**
     * Failed to deliver after reaching the maximum number of retry attempts.
     */
    FAILED
}