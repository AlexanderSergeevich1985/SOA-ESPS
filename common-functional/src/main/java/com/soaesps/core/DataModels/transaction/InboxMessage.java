package com.soaesps.core.DataModels.transaction;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an Inbox pattern record used to ensure exactly-once processing logs.
 * Persisted within the same business transaction to achieve strict idempotency.
 */
@Entity
@Table(name = "inbox_messages")
public class InboxMessage {

    @Id
    @Column(name = "message_id")
    private String messageId; // The unique ID from the producer's Outbox table

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    /**
     * Default constructor required by JPA.
     */
    protected InboxMessage() {
        // Required by JPA specifications
    }

    /**
     * Constructs a new Inbox record log.
     *
     * @param messageId the unique identifier of the incoming event
     */
    public InboxMessage(String messageId) {
        this.messageId = messageId;
        this.processedAt = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}