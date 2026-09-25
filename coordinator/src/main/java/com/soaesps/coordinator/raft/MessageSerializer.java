package com.soaesps.coordinator.raft;

import com.soaesps.coordinator.domain.GroupMessage;

import java.util.List;

/**
 * Abstraction for serializing/deserializing GroupMessage batches
 * to/from byte arrays for Raft log replication.
 */
public interface MessageSerializer {

    /**
     * Serializes a batch of messages into a single byte array
     * for atomic Raft log entry.
     *
     * @param batch The list of messages to serialize.
     * @return The serialized byte array.
     */
    byte[] serializeBatch(List<GroupMessage> batch);

    /**
     * Deserializes a byte array back into a single GroupMessage.
     * Used by the State Machine when applying committed transactions.
     *
     * @param data The serialized byte array.
     * @return The deserialized GroupMessage.
     */
    GroupMessage deserialize(byte[] data);

    /**
     * Deserializes a byte array back into a list of GroupMessages.
     * Used when replaying a batch from the Raft log.
     *
     * @param data The serialized byte array.
     * @return The deserialized list of GroupMessages.
     */
    List<GroupMessage> deserializeBatch(byte[] data);
}