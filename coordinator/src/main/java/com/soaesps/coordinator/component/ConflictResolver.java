package com.soaesps.coordinator.component;

import com.soaesps.coordinator.domain.GroupMessage;
import org.springframework.stereotype.Component;

@Component
public class ConflictResolver {

    /**
     * Resolve conflicts when two users simultaneously update the same message
     * Uses Last-Writer-Wins (LWW) strategy with deterministic tie-breaker
     */
    public GroupMessage resolve(GroupMessage a, GroupMessage b) {
        // Check causal ordering
        if (a.getVectorClock().happensBefore(b.getVectorClock())) {
            return b; // b is newer
        }
        if (b.getVectorClock().happensBefore(a.getVectorClock())) {
            return a; // a is newer
        }

        // Concurrent updates - use LWW with timestamp
        if (a.getTimestamp() != b.getTimestamp()) {
            return a.getTimestamp() > b.getTimestamp() ? a : b;
        }

        // Deterministic tie-breaker by sender ID
        return a.getSenderId().compareTo(b.getSenderId()) < 0 ? a : b;
    }
}