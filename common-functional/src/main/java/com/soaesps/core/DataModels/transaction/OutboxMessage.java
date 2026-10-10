package com.soaesps.core.DataModels.transaction;

import com.soaesps.core.dto.MessageStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Represents an Outbox pattern message used for reliable event publishing.
 * <p>
 * @note All fields except {@code status} are immutable to ensure event data integrity.
 */
@Entity
@Table(name = "OUTBOX_MESSAGES")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String aggregateId;
    private String eventType;
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json") // or text
    private String payload;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private int retryCount = 0;
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    protected OutboxMessage() {
        // Required by JPA specifications
    }

    /**
     * Constructs a new immutable outbox message with a default PENDING status.
     *
     * @param aggregateId the ID of the related aggregate root
     * @param eventType   the type of the event being published
     * @param topic       the target messaging topic for routing
     * @param payload     the serialized event data payload
     */
    public OutboxMessage(String aggregateId, String eventType, String topic, String payload) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.status = MessageStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * Automatically sets creation and modification timestamps before inserting into database.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
    }

    // ==========================================
    // Getters (Available for all fields)
    // ==========================================

    /**
     * Gets the unique identifier of the outbox message.
     * @return the message ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID of the aggregate root associated with this event.
     * @return the aggregate ID
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Gets the type of the event.
     * @return the event type
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Gets the target messaging topic or exchange for routing.
     * @return the target topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the serialized event data payload.
     * @return the JSON or text payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Gets the current processing status of the message.
     * @return the message status
     */
    public MessageStatus getStatus() {
        return status;
    }

    /**
     * Gets the number of delivery attempts made.
     * @return the retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Gets the timestamp when the message was created.
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ==========================================
    // Setters (Only status modification is allowed)
    // ==========================================

    /**
     * Updates the current processing status of the message.
     * This is the only mutable field allowed to track processing state transitions.
     *
     * @param status the new message status to set
     */
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}