package com.soaesps.coordinator.component;

import com.soaesps.coordinator.domain.GroupMessage;
import java.util.List;

/**
 * High-level abstraction layer responsible for deterministic N-way state
 * conflict resolution inside incoming message synchronization batches.
 */
public interface ConflictResolverI {

    /**
     * Evaluates a collection of messages, groups them by entity target keys,
     * and filters out concurrent updates using Last-Writer-Wins constraints.
     *
     * @param batch The causally ordered list of incoming messages.
     * @return A list containing only the winning decoupled state updates.
     */
    List<GroupMessage> resolveBatch(List<GroupMessage> batch);
}