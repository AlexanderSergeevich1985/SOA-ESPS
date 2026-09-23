package com.soaesps.coordinator.dag;

import com.soaesps.coordinator.domain.GroupMessage;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * High-performance abstraction layer responsible for constructing in-memory DAG topologies,
 * performing causal serialization (linearization), and evaluating logical vector dependencies.
 */
public interface CausalDagProcessorI {

    /**
     * Transforms a raw batch of incoming messages into a strictly sequential,
     * linearized list using topological sort configurations.
     *
     * @param batch Collection of raw, unsorted group messages.
     * @return A causally ordered list of messages ready for transactional processing.
     */
    List<GroupMessage> linearizeBatch(Collection<GroupMessage> batch);

    /**
     * Correct happens-before evaluation per Lamport's partial ordering definitions.
     *
     * @param clockA Vector clock state mapping for transaction context A.
     * @param clockB Vector clock state mapping for transaction context B.
     * @return true if event A causally preceded event B.
     */
    boolean isHappenedBefore(Map<String, Long> clockA, Map<String, Long> clockB);

    /**
     * Evaluates if two execution contexts are concurrent (neither logically preceded the other).
     *
     * @param clockA Vector clock state mapping for transaction context A.
     * @param clockB Vector clock state mapping for transaction context B.
     * @return true if events are concurrent.
     */
    boolean isConcurrent(Map<String, Long> clockA, Map<String, Long> clockB);

    /**
     * Updates the global causality frontier index caches following successful log commits.
     *
     * @param committed The finalized message payload confirmed by the cluster quorum.
     */
    void updateFrontier(GroupMessage committed);
}