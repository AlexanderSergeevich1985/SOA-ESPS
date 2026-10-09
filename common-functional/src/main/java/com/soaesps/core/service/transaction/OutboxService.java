package com.soaesps.core.service.transaction;

import com.soaesps.core.dto.MessageStatus;
import com.soaesps.core.DataModels.transaction.OutboxMessage;
import com.soaesps.core.repository.transaction.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;

/**
 * Service responsible for managing the lifecycle of outbox messages,
 * ensuring transactional event publishing (Outbox Pattern).
 */
@Service
public class OutboxService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxMessageRepository outboxRepository,
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes an outbox message to the message broker (Kafka) asynchronously
     * and updates its processing state based on the result.
     *
     * @param message the outbox message event to send
     */
    public void sendMessage(OutboxMessage message) {
        kafkaTemplate.send(message.getTopic(), message.getAggregateId(), message.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        outboxRepository.updateStatus(message.getId(), MessageStatus.COMPLETED);
                        logger.info("Outbox message {} sent successfully to topic {}",
                                message.getId(), message.getTopic());
                    } else {
                        logger.error("Failed to send outbox message {} to topic {}",
                                message.getId(), message.getTopic(), ex);
                        outboxRepository.incrementRetryCount(message.getId());
                    }
                });
    }

    /**
     * Retrieves a limited batch of pending messages to be processed by a background scheduler.
     *
     * @param limit the maximum number of messages to fetch
     * @return a list of pending outbox messages sorted by creation time
     */
    @Transactional(readOnly = true)
    public List<OutboxMessage> getPendingMessages(int limit) {
        return outboxRepository.findByStatusOrderByCreatedAtAsc(MessageStatus.PENDING, PageRequest.of(0, limit));
    }

    /**
     * Marks an outbox message as FAILED when it exceeds the maximum delivery retry threshold.
     *
     * @param messageId the unique identifier of the failed message
     */
    @Transactional
    public void markAsFailed(String messageId) {
        outboxRepository.updateStatus(messageId, MessageStatus.FAILED);
        logger.warn("Outbox message {} marked as FAILED after max retries", messageId);
    }

    /**
     * Saves a critical event message into the outbox table within the current database transaction.
     * This method enforces strict transactional context to prevent data inconsistency.
     *
     * @param aggregateId the ID of the related aggregate root
     * @param eventType   the type of the domain event (used for downstream routing/deserialization)
     * @param topic       the target messaging topic
     * @param payload     the raw object payload to serialize into JSON
     * @param <T>         the type of the payload object
     * @return the persisted outbox message entity
     * @throws IllegalStateException if called outside an active transaction
     * @throws RuntimeException if JSON serialization fails
     */
    @Transactional
    public <T> OutboxMessage saveMessage(String aggregateId, String eventType, String topic, T payload) {
        // Enforce strict business transaction alignment (Fail-Fast)
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("saveMessage must be called within an active @Transactional context");
        }

        try {
            // Serialize any incoming generic object contract to raw string data
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxMessage message = new OutboxMessage(aggregateId, eventType, topic, payloadJson);
            message.setStatus(MessageStatus.PENDING);

            return outboxRepository.save(message);
        } catch (Exception e) {
            logger.error("Failed to serialize outbox message for aggregate: {}", aggregateId, e);
            throw new RuntimeException("Outbox message serialization failed", e);
        }
    }

    /**
     * Saves a critical event to the outbox table and automatically triggers
     * its asynchronous dispatch to Kafka immediately after the transaction commits.
     *
     * @param aggregateId the ID of the related aggregate root
     * @param eventType   the type of the domain event
     * @param topic       the target messaging topic
     * @param payload     the raw object payload to serialize
     * @param <T>         the type of the payload object
     */
    @Transactional
    public <T> void saveAndPublishAfterCommit(String aggregateId, String eventType, String topic, T payload) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("saveAndPublishAfterCommit must be called within an active @Transactional context");
        }

        final OutboxMessage savedMessage = saveMessage(aggregateId, eventType, topic, payload);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendMessage(savedMessage);
            }
        });
    }
}