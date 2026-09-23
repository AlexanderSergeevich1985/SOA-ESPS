package com.soaesps.coordinator.domain;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable data representation of a message within the distributed system.
 * Maximum usage of Lombok for builder pattern and seamless Jackson serialization.
 */
@Jacksonized
@Builder(toBuilder = true)
public record GroupMessage(
        String id,
        String groupId,
        String senderId,
        String entityKey,
        String content,
        long timestamp,
        long iterationMarker,
        VectorClock vectorClock,
        List<String> parentIds,
        MessageType type,
        String replyToId
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 4L;

    /**
     * Compact constructor for canonical validation and defensive copying.
     */
    public GroupMessage {
        Objects.requireNonNull(id, "Message ID cannot be null");
        Objects.requireNonNull(entityKey, "Entity key cannot be null");
        Objects.requireNonNull(senderId, "Sender ID cannot be null");

        // Defensive copy to guarantee strict immutability invariants
        parentIds = parentIds == null ? List.of() : List.copyOf(parentIds);

        // DoS protection against unbounded vector clocks allocation (from Viktorov's paper context)
        if (vectorClock != null && vectorClock.getClocks() != null && vectorClock.getClocks().size() > 1024) {
            throw new IllegalArgumentException("Vector clock size exceeds DoS limit: " + vectorClock.getClocks().size());
        }
    }

    public enum MessageType {
        TEXT, EDIT, DELETE, REACTION, SYSTEM
    }

    /**
     * Custom equality by business key only to handle deduplication correctly in concurrent buffers.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMessage that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}