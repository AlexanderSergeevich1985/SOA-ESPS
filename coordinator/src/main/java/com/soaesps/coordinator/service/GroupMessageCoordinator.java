package com.soaesps.coordinator.service;

import com.soaesps.coordinator.component.ConflictResolver;
import com.soaesps.coordinator.domain.DeliveryReceipt;
import com.soaesps.coordinator.domain.GroupMessage;
import com.soaesps.coordinator.domain.VectorClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core coordinator service responsible for synchronizing group messages
 * across distributed data centers.
 *
 * Adapts the "iteration" concept from the research article but replaces
 * the particle filter with causal sorting (Vector Clocks) for correctness.
 *
 * Synchronization cycle:
 *   1. Prepare phase  - causal sort + conflict resolution
 *   2. Commit phase   - replicate to Kafka / downstream nodes
 *   3. Notify phase   - send delivery receipts to clients
 */
@Slf4j
@Service
public class GroupMessageCoordinator {

    private final KafkaTemplate<String, GroupMessage> kafkaTemplate;
    private final ConflictResolver conflictResolver;
    private final VectorClockService vectorClockService;

    /**
     * Global iteration counter for batch synchronization.
     * Each iteration corresponds to one synchronization cycle.
     */
    private final AtomicLong currentIteration = new AtomicLong(0);

    /**
     * Buffer holding messages grouped by their assigned iteration marker.
     * Thread-safe: CopyOnWriteArrayList allows concurrent writes.
     */
    private final Map<Long, List<GroupMessage>> iterationBuffer = new ConcurrentHashMap<>();

    /**
     * Constructor injection of required dependencies.
     */
    public GroupMessageCoordinator(
            KafkaTemplate<String, GroupMessage> kafkaTemplate,
            ConflictResolver conflictResolver,
            VectorClockService vectorClockService) {
        this.kafkaTemplate = kafkaTemplate;
        this.conflictResolver = conflictResolver;
        this.vectorClockService = vectorClockService;
    }

    // =================================================================
    // PUBLIC API
    // =================================================================

    /**
     * Accepts an incoming message from a client or API gateway.
     * Assigns an iteration marker and updates the group's vector clock.
     *
     * @param message The incoming group message to be synchronized.
     */
    public DeliveryReceipt acceptMessage(GroupMessage message) {

        // 1. Assign the current iteration marker (as per the article's model)
        long iteration = currentIteration.get();
        message.setIterationMarker(iteration);

        // 2. Atomically increment and retrieve the vector clock via Redis
        VectorClock updatedClock = vectorClockService.incrementAndGet(
                message.getGroupId(),
                message.getSenderId()
        );
        message.setVectorClock(updatedClock);

        // 3. Buffer the message for the next synchronization cycle
        iterationBuffer
                .computeIfAbsent(iteration, k -> new CopyOnWriteArrayList<>())
                .add(message);

        log.debug("Message {} accepted and buffered for iteration {}",
                message.getId(), iteration);

        return new DeliveryReceipt(
                message.getId(),
                1,
                DeliveryReceipt.DeliveryStatus.PENDING,
                iteration
        );
    }

    // =================================================================
    // SYNCHRONIZATION CYCLE (scheduled)
    // =================================================================

    /**
     * Synchronization cycle triggered periodically (every 100 ms).
     * Implements a simplified 2-phase approach:
     *   - Prepare: causal sort + conflict resolution
     *   - Commit:  replicate to Kafka
     *   - Notify:  send acknowledgments
     */
    @Scheduled(fixedDelay = 100)
    public void synchronizationCycle() {

        long iteration = currentIteration.getAndIncrement();
        List<GroupMessage> batch = iterationBuffer.remove(iteration);

        if (batch == null || batch.isEmpty()) {
            return; // Nothing to process in this iteration
        }

        log.info("Starting synchronization cycle for iteration {}", iteration);

        // === Prepare phase (from the article) ===
        List<GroupMessage> ordered = causalSort(batch);
        List<GroupMessage> resolved = resolveConflicts(ordered);

        // === Commit phase (from the article) ===
        commitToReplicas(iteration, resolved);

        // === Notify phase ===
        notifyClients(iteration, resolved);

        log.info("Completed synchronization cycle for iteration {}. "
                        + "Processed {} messages.",
                iteration, resolved.size());
    }

    // =================================================================
    // PRIVATE HELPERS
    // =================================================================

    /**
     * Causal sorting - critical for messages but missing in the article.
     * Ensures cause-and-effect order is preserved using vector clocks.
     *
     * @param messages The unsorted batch of messages.
     * @return A causally ordered list of messages.
     */
    private List<GroupMessage> causalSort(List<GroupMessage> messages) {
        List<GroupMessage> sorted = new ArrayList<>(messages);
        sorted.sort((GroupMessage a, GroupMessage b) -> {
            if (a.getVectorClock().happensBefore(b.getVectorClock())) {
                return -1;
            }
            if (b.getVectorClock().happensBefore(a.getVectorClock())) {
                return 1;
            }
            // Concurrent events: tie-breaker using physical timestamp,
            // then senderId for determinism
            int timeCmp = Long.compare(a.getTimestamp(), b.getTimestamp());
            return timeCmp != 0
                    ? timeCmp
                    : a.getSenderId().compareTo(b.getSenderId());
        });
        return sorted;
    }

    /**
     * Resolves conflicts for messages that target the same resource
     * (e.g., two users editing the same message concurrently).
     * Delegates to the injected {@link ConflictResolver}.
     *
     * @param messages The causally sorted messages.
     * @return The list of messages after conflict resolution.
     */
    private List<GroupMessage> resolveConflicts(List<GroupMessage> messages) {
        // Group messages by a logical key (e.g., replyToId or content hash)
        // to detect concurrent updates to the same resource.
        // For the baseline implementation we run pairwise resolution
        // across the entire batch.

        Map<String, GroupMessage> resolvedByKey = new ConcurrentHashMap<>();

        for (GroupMessage msg : messages) {
            String key = resolveKey(msg);
            resolvedByKey.merge(key, msg, conflictResolver::resolve);
        }

        return new ArrayList<>(resolvedByKey.values());
    }

    /**
     * Computes a logical key for conflict detection.
     * Messages that share the same key are considered to target
     * the same resource and may require conflict resolution.
     *
     * @param msg The message to compute the key for.
     * @return A string key representing the target resource.
     */
    private String resolveKey(GroupMessage msg) {
        // Default: each message is unique by its own ID.
        // Override this logic if you need to detect edits/deletes
        // on the same original message (e.g., use replyToId or a
        // dedicated "targetMessageId" field).
        return msg.getId();
    }

    /**
     * Replicates the finalized batch of messages to the underlying
     * storage/messaging layer (Kafka).
     *
     * @param iteration The current iteration marker.
     * @param messages  The resolved and ordered messages.
     */
    private void commitToReplicas(long iteration, List<GroupMessage> messages) {
        for (GroupMessage message : messages) {
            String topic = "group.events." + message.getGroupId();
            kafkaTemplate.send(topic, message.getId(), message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to replicate message {} "
                                            + "in iteration {}",
                                    message.getId(), iteration, ex);
                        } else {
                            log.debug("Successfully replicated message {} "
                                            + "to Kafka",
                                    message.getId());
                        }
                    });
        }
    }

    /**
     * Sends delivery acknowledgments to clients for all messages
     * processed in the given iteration.
     *
     * In a production system this would push receipts via WebSocket,
     * SSE, or a dedicated response Kafka topic.
     *
     * @param iteration The iteration marker.
     * @param messages  The messages that were successfully committed.
     */
    private void notifyClients(long iteration, List<GroupMessage> messages) {
        for (GroupMessage message : messages) {
            DeliveryReceipt receipt = new DeliveryReceipt(
                    message.getId(),
                    1, // replica count (update when multi-DC is live)
                    DeliveryReceipt.DeliveryStatus.DELIVERED,
                    iteration
            );
            log.debug("Delivery receipt for message {}: {}",
                    message.getId(), receipt);
            // TODO: push receipt to client via WebSocket / SSE / response topic
        }
    }
}