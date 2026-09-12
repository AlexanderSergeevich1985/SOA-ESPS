package com.soaesps.coordinator.domain;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vector clocks for causal ordering of messages.
 * Critical for maintaining cause-and-effect relationships in group chats.
 */
@Getter
@ToString
public class VectorClock implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Long> clocks;

    private final Map<String, VectorClock> groupClocks = new ConcurrentHashMap<>();

    public VectorClock() {
        this.clocks = new ConcurrentHashMap<>();
    }

    /**
     * Increment the clock for a specific node
     */
    public void increment(String nodeId) {
        clocks.merge(nodeId, 1L, Long::sum);
    }

    /**
     * Merge with another vector clock (take max for each node)
     */
    public void merge(VectorClock other) {
        other.clocks.forEach((node, ts) ->
                clocks.merge(node, ts, Math::max)
        );
    }

    /**
     * Retrieves or creates a VectorClock instance for the specified group.
     * Alignment with the thesis: Provides the baseline clock for the coordination iteration.
     */
    public VectorClock getGroupClock(String groupId) {
        return groupClocks.computeIfAbsent(groupId, id -> new VectorClock());
    }

    /**
     * Check if this clock causally happens-before another clock
     */
    public boolean happensBefore(VectorClock other) {
        boolean atLeastOneLess = false;

        // Check all keys in this clock
        for (Map.Entry<String, Long> e : clocks.entrySet()) {
            long otherVal = other.clocks.getOrDefault(e.getKey(), 0L);
            if (e.getValue() > otherVal) return false;
            if (e.getValue() < otherVal) atLeastOneLess = true;
        }

        // Check keys that exist only in other clock
        for (Map.Entry<String, Long> e : other.clocks.entrySet()) {
            if (!clocks.containsKey(e.getKey()) && e.getValue() > 0) {
                atLeastOneLess = true;
            }
        }

        return atLeastOneLess;
    }

    /**
     * Check if two clocks are concurrent (neither happens-before the other)
     */
    public boolean isConcurrent(VectorClock other) {
        return !this.happensBefore(other)
                && !other.happensBefore(this)
                && !this.equals(other);
    }

    /**
     * Custom equals to handle missing keys as 0L consistently
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorClock that = (VectorClock) o;

        // Merge keys from both maps for consistent validation
        return java.util.stream.Stream.concat(clocks.keySet().stream(), that.clocks.keySet().stream())
                .distinct()
                .allMatch(key -> java.util.Objects.equals(this.clocks.getOrDefault(key, 0L), that.clocks.getOrDefault(key, 0L)));
    }

    @Override
    public int hashCode() {
        // Since the map is mutable, use hashCode in HashSet/HashMap with extreme caution!
        // Calculation accounts for non-zero elements only
        return clocks.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .mapToInt(e -> java.util.Objects.hash(e.getKey(), e.getValue()))
                .sum();
    }
}