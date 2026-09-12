package com.soaesps.coordinator.domain;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a message within a group chat or distributed system.
 * Includes metadata for iteration tracking (from the article) and causal ordering.
 */
@Data
@Builder
public class GroupMessage implements Serializable {

    /** Unique identifier for the message (e.g., UUID). */
    private String id;

    /** Identifier of the group or chat room. */
    private String groupId;

    /** Identifier of the user who sent the message. */
    private String senderId;

    /** The actual content or payload of the message. */
    private String content;

    /** Physical wall-clock timestamp of message creation. */
    private long timestamp;

    /**
     * Iteration marker assigned by the coordinator.
     * Used for batch synchronization phases (Prepare/Commit).
     */
    private long iterationMarker;

    /** Vector clock representing the causal state at the time of sending. */
    private VectorClock vectorClock;

    /** Type of operation: TEXT, EDIT, DELETE, REACTION, etc. */
    private MessageType type;

    /** Optional: ID of the message this is replying to (for threading). */
    private String replyToId;

    public enum MessageType {
        TEXT, EDIT, DELETE, REACTION, SYSTEM
    }
}