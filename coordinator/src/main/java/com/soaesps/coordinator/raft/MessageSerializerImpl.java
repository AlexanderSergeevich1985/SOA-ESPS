package com.soaesps.coordinator.raft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.coordinator.domain.GroupMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Jackson-based implementation of MessageSerializer.
 * Uses JSON for human-readable debugging and schema evolution.
 */
@Component
public class MessageSerializerImpl implements MessageSerializer {

    private static final Logger log = LoggerFactory.getLogger(MessageSerializerImpl.class);

    private final ObjectMapper objectMapper;

    public MessageSerializerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serializeBatch(List<GroupMessage> batch) {
        try {
            return objectMapper.writeValueAsBytes(batch);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize batch of {} messages", batch.size(), e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @Override
    public GroupMessage deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, GroupMessage.class);
        } catch (IOException e) {
            log.error("Failed to deserialize single GroupMessage from {} bytes", data.length, e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public List<GroupMessage> deserializeBatch(byte[] data) {
        try {
            return objectMapper.readValue(data, new TypeReference<List<GroupMessage>>() {});
        } catch (IOException e) {
            log.error("Failed to deserialize batch from {} bytes", data.length, e);
            throw new RuntimeException("Batch deserialization failed", e);
        }
    }
}