package com.soaesps.coordinator.component;

import com.soaesps.coordinator.domain.GroupMessage;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConflictResolver implements ConflictResolverI {

    /**
     * Resolves N-way conflicts within a batch in a single pass O(N).
     * Groups messages by entityKey and keeps only the winning message per entity.
     * Losers are implicitly dropped (in production, they can be routed to a DLQ here).
     *
     * @param batch The linearized batch of messages.
     * @return List of winning messages, one per unique entityKey.
     */
    @Override
    public List<GroupMessage> resolveBatch(List<GroupMessage> batch) {
        if (batch == null || batch.isEmpty()) {
            return List.of();
        }

        // Single-pass resolution: map stores only the current winner for each entity.
        // Initial capacity is set to batch size to avoid map resizing.
        Map<String, GroupMessage> winnersByEntity = new HashMap<>(batch.size());

        for (GroupMessage msg : batch) {
            String entityKey = msg.entityKey();
            GroupMessage currentWinner = winnersByEntity.get(entityKey);

            if (currentWinner == null) {
                // First message for this entity becomes the provisional winner
                winnersByEntity.put(entityKey, msg);
            } else {
                // Deterministic LWW resolution against the current winner
                GroupMessage newWinner = pickWinner(currentWinner, msg);

                // Only update the map if the new message actually won
                if (newWinner != currentWinner) {
                    winnersByEntity.put(entityKey, newWinner);
                }
            }
        }

        // Return the final consolidated list of winners
        return new ArrayList<>(winnersByEntity.values());
    }

    /**
     * Deterministic LWW: physical time first, then senderId lexicographically, then message id.
     * Guarantees all nodes converge to same winner — critical for Raft state machine consistency.
     */
    private GroupMessage pickWinner(GroupMessage a, GroupMessage b) {
        int cmp = Long.compare(a.timestamp(), b.timestamp());
        if (cmp != 0) return cmp > 0 ? a : b;

        cmp = a.senderId().compareTo(b.senderId());
        if (cmp != 0) return cmp > 0 ? a : b;

        return a.id().compareTo(b.id()) > 0 ? a : b;
    }
}