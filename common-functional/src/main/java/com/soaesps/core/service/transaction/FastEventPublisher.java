package com.soaesps.core.service.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.DataModels.transaction.FailedOutboxEvent;
import com.soaesps.core.repository.transaction.FailedOutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Universal service for safely publishing non-critical events to Kafka
 * strictly after the current database transaction successfully commits.
 */
@Service
public class FastEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(FastEventPublisher.class);

    @Autowired
    @Lazy
    private FastEventPublisher self; // Self-proxy reference to execute REQUIRES_NEW transaction

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;
    private final FailedOutboxEventRepository failedOutboxRepository;

    public FastEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                              TaskExecutor taskExecutor,
                              ObjectMapper objectMapper, FailedOutboxEventRepository failedOutboxRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
        this.failedOutboxRepository = failedOutboxRepository;
    }

    /**
     * Schedules a Kafka message to be sent asynchronously after the active transaction commits.
     * If no transaction is active, sends the message immediately.
     *
     * @param topic      the target Kafka topic
     * @param routingKey the partition/routing key (e.g., username or aggregate ID)
     * @param payload    the event data object to be serialized into JSON
     * @param <T>        the type of the payload object
     */
    public <T> void publishAfterCommit(String topic, String routingKey, T payload) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("publishAfterCommit must be called within an active @Transactional context");
        }

        final String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            logger.error("Failed to serialize event payload for topic: {} and key: {}", topic, routingKey, e);
            throw new RuntimeException("Event serialization failed", e);
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeAsyncSend(topic, routingKey, jsonPayload);
                }
            });
        } else {
            logger.warn("No active transaction found. Dispatching event to topic '{}' immediately.", topic);
            executeAsyncSend(topic, routingKey, jsonPayload);
        }
    }

    private void executeAsyncSend(String topic, String routingKey, String jsonPayload) {
        taskExecutor.execute(() -> {
            kafkaTemplate.send(topic, routingKey, jsonPayload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.warn("Non-critical event dropped for topic '{}', routingKey: '{}'. Error: {}",
                                    topic, routingKey, ex.getMessage());
                            self.saveToFailedOutbox(routingKey, "BULK_NOTIFICATION_UPDATE_FAILED", jsonPayload);
                        } else {
                            logger.debug("Successfully routed event to topic '{}' for key '{}'", topic, routingKey);
                        }
                    });
        });
    }

    /**
     * Creates an isolated historical outbox record when the message broker layer cannot accept the stream.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToFailedOutbox(String aggregateId, String eventType, String payload) {
        FailedOutboxEvent event = new FailedOutboxEvent();
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        this.failedOutboxRepository.save(event);
    }
}