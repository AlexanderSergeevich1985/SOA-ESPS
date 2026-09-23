package com.soaesps.coordinator.raft;

import com.soaesps.coordinator.domain.GroupMessage;
import java.util.List;

/**
 * Abstraction layer responsible for encoding message batches into raw byte arrays
 * (e.g., via Protobuf serialization) prior to Raft log execution.
 */
public interface MessageSerializer {

    /**
     * Serializes a structured batch of messages into a single contiguous byte array.
     *
     * @param batch The causally ordered list of messages.
     * @return The serialized payload byte array.
     * @throws Exception If serialization constraints fail.
     */
    byte[] serializeBatch(List<GroupMessage> batch) throws Exception;
}