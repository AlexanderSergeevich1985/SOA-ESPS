package com.soaesps.coordinator.dag;

import com.soaesps.coordinator.domain.GroupMessage;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance DAG processor for causal message linearization.
 */
@Component
public class CausalDagProcessor implements CausalDagProcessorI {

    private static final Logger log = LoggerFactory.getLogger(CausalDagProcessor.class);

    // Frontier cache: tracks the vector clock state of the last committed messages per entity.
    private final ConcurrentHashMap<String, CausalSnapshot> frontier = new ConcurrentHashMap<>();

    // Reverse index for O(1) lookup of committed message IDs.
    // Memory footprint is strictly bounded by the number of unique entityKeys in the frontier.
    private final ConcurrentHashMap<String, String> frontierMessageIndex = new ConcurrentHashMap<>();

    public List<GroupMessage> linearizeBatch(Collection<GroupMessage> batch) {
        if (batch == null || batch.isEmpty()) return Collections.emptyList();

        // Index batch by ID for O(1) parent lookup within the current batch
        Map<String, GroupMessage> messageIndex = new HashMap<>(batch.size());
        for (GroupMessage msg : batch) {
            if (msg.id() != null) {
                messageIndex.put(msg.id(), msg);
            }
        }

        DirectedAcyclicGraph<String, DefaultEdge> dag = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (String msgId : messageIndex.keySet()) {
            dag.addVertex(msgId);
        }

        int droppedEdges = 0;
        for (GroupMessage msg : batch) {
            if (msg.parentIds() == null) continue;

            for (String parentId : msg.parentIds()) {
                boolean parentInBatch = messageIndex.containsKey(parentId);

                if (parentInBatch) {
                    try {
                        dag.addEdge(parentId, msg.id());
                    } catch (IllegalArgumentException cycle) {
                        log.warn("Causal cycle detected: {} -> {}. Dropping structural edge to preserve DAG invariant.", parentId, msg.id());
                        droppedEdges++;
                    }
                } else {
                    // O(1) lookup in the reverse index instead of O(N) stream traversal
                    boolean parentInFrontier = frontierMessageIndex.containsKey(parentId);

                    if (!parentInFrontier) {
                        log.warn("Orphan parent reference detected: {} -> {}. Causal chain broken (parent not in batch nor frontier).", parentId, msg.id());
                    }
                    // Note: If parentInFrontier is true, no edge is needed.
                    // The parent is already globally committed, so its causal precedence is implicitly guaranteed.
                }
            }
        }

        if (droppedEdges > 0) {
            log.warn("Dropped {} structural causal edges due to cycle safety invariants within this batch", droppedEdges);
        }

        // Topological sort = linearization of the partial order
        List<GroupMessage> sorted = new ArrayList<>(batch.size());
        TopologicalOrderIterator<String, DefaultEdge> it = new TopologicalOrderIterator<>(dag);
        while (it.hasNext()) {
            GroupMessage msg = messageIndex.get(it.next());
            if (msg != null) sorted.add(msg);
        }

        return sorted;
    }

    /**
     * Correct happens-before check per Lamport's definition.
     * A -> B iff (forall k: A[k] <= B[k]) AND (exists k: A[k] < B[k])
     */
    public boolean isHappenedBefore(Map<String, Long> clockA, Map<String, Long> clockB) {
        if (clockA == null || clockB == null) return false;

        boolean atLeastOneLess = false;

        // 1. Check all keys present in A against B
        for (Map.Entry<String, Long> e : clockA.entrySet()) {
            long bVal = clockB.getOrDefault(e.getKey(), 0L);
            if (e.getValue() > bVal) {
                return false; // Violation: A[k] > B[k] means A cannot happen before B
            }
            if (e.getValue() < bVal) {
                atLeastOneLess = true;
            }
        }

        // 2. Check ONLY keys that exist in B but are completely missing from A
        // (Implicitly, A's value for these keys is 0)
        for (Map.Entry<String, Long> e : clockB.entrySet()) {
            if (!clockA.containsKey(e.getKey()) && e.getValue() > 0) {
                atLeastOneLess = true;
                break; // Optimization shortcut: found at least one smaller dimension
            }
        }

        return atLeastOneLess;
    }

    /**
     * Checks if two events are concurrent (neither happened before the other).
     */
    public boolean isConcurrent(Map<String, Long> clockA, Map<String, Long> clockB) {
        return !isHappenedBefore(clockA, clockB)
                && !isHappenedBefore(clockB, clockA)
                && !Objects.equals(clockA, clockB);
    }

    /**
     * Updates the frontier reference safely upon successful transactional Raft commits.
     * Maintains both the frontier state and the O(1) reverse index atomically.
     */
    public void updateFrontier(GroupMessage committed) {
        if (committed.entityKey() == null || committed.vectorClock() == null) return;

        // Defensive copy to guarantee immutability, protecting against external map mutations
        Map<String, Long> immutableClocks = Map.copyOf(committed.vectorClock().getClocks());
        CausalSnapshot newSnapshot = new CausalSnapshot(committed.id(), immutableClocks);

        // Use compute() for atomic read-modify-write of both maps
        frontier.compute(committed.entityKey(), (entityKey, oldSnapshot) -> {
            if (oldSnapshot == null || isHappenedBefore(oldSnapshot.clocks(), newSnapshot.clocks())) {
                // New snapshot is strictly newer (or first). Update the reverse index.
                if (oldSnapshot != null) {
                    frontierMessageIndex.remove(oldSnapshot.messageId());
                }
                frontierMessageIndex.put(newSnapshot.messageId(), entityKey);
                return newSnapshot;
            }
            // Old snapshot is newer or concurrent. Keep old, do not modify the reverse index.
            return oldSnapshot;
        });
    }

    /**
     * Micro-snapshot record to eliminate heap memory bloat within the long-running cache.
     */
    private record CausalSnapshot(String messageId, Map<String, Long> clocks) {}
}