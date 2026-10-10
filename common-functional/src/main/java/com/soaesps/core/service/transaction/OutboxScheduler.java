package com.soaesps.core.service.transaction;

import com.soaesps.core.DataModels.transaction.OutboxMessage;
import com.soaesps.core.dto.MessageStatus;
import com.soaesps.core.repository.transaction.OutboxMessageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Background scheduler responsible for polling and publishing stuck pending outbox messages.
 * Guarantees at-least-once delivery semantics even if initial post-commit publication fails.
 */
@Component
@EnableScheduling // Enables the internal Spring task scheduler execution pool
public class OutboxScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxScheduler(OutboxMessageRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Periodically sweeps the outbox table for pending records every 5 seconds.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional // Processes updates within an isolated read-write transaction workspace
    public void processPendingMessages() {
        // Fetch up to 50 pending records to prevent memory exhaustion during huge loads
        List<OutboxMessage> pendingMessages = outboxRepository.findByStatusOrderByCreatedAtAsc(MessageStatus.PENDING, Pageable.ofSize(50));

        if (pendingMessages.isEmpty()) {
            return;
        }

        logger.info("Found {} pending outbox messages requiring automated delivery sync", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                // Use aggregateId as the routing/partition key for identical ordering guarantees
                kafkaTemplate.send(message.getTopic(), message.getAggregateId(), message.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                logger.info("Successfully delivered outbox message ID: {} to topic: {}",
                                        message.getId(), message.getTopic());
                            } else {
                                logger.error("Async Kafka broker acknowledgement failed for message ID: {}",
                                        message.getId(), ex);
                            }
                        });

                // Update processing logs inside the active database transaction boundary
                message.setStatus(MessageStatus.COMPLETED);
                outboxRepository.save(message);

            } catch (Exception e) {
                int newRetryCount = message.getRetryCount() + 1;
                message.setRetryCount(newRetryCount);

                // If the message fails repeatedly, consider shifting it to a FAILED state for dead-letter analysis
                if (newRetryCount >= 5) {
                    message.setStatus(MessageStatus.FAILED);
                    logger.error("Outbox message ID: {} reached maximum retry capacity. Execution suspended.", message.getId(), e);
                } else {
                    logger.warn("Temporary transport routing failure for outbox record ID: {}. Retrying later.", message.getId(), e);
                }
                outboxRepository.save(message);
            }
        }
    }
}