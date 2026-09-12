package com.soaesps.coordinator.service;

import com.soaesps.coordinator.domain.VectorClock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for managing distributed Vector Clocks using Redis.
 * Utilizes Redis Hashes for atomic increments and retrieval, preventing
 * race conditions during concurrent message processing across coordinator nodes.
 */
@Service
public class VectorClockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CLOCK_KEY_PREFIX = "vc:group:";

    public VectorClockService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atomically increments the vector clock for a specific group and sender,
     * then retrieves the updated clock state.
     *
     * @param groupId  The identifier of the group.
     * @param senderId The identifier of the sender (acts as the dimension in the vector clock).
     * @return The updated VectorClock representing the current causal state.
     */
    public VectorClock incrementAndGet(String groupId, String senderId) {
        String key = CLOCK_KEY_PREFIX + groupId;

        // Atomically increment the counter for this specific sender in the group's vector clock
        redisTemplate.opsForHash().increment(key, senderId, 1L);

        // Retrieve the entire hash map to construct the full VectorClock
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        return buildVectorClock(entries);
    }

    /**
     * Retrieves the current Vector Clock for a group without incrementing.
     * Useful for read-only operations or initializing local state.
     *
     * @param groupId The identifier of the group.
     * @return The current VectorClock, or a new empty one if it doesn't exist yet.
     */
    public VectorClock getGroupClock(String groupId) {
        String key = CLOCK_KEY_PREFIX + groupId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return buildVectorClock(entries);
    }

    /**
     * Helper method to convert Redis Hash entries into a VectorClock instance.
     *
     * @param entries The raw map from Redis.
     * @return A populated VectorClock object.
     */
    private VectorClock buildVectorClock(Map<Object, Object> entries) {
        VectorClock clock = new VectorClock();
        if (entries != null && !entries.isEmpty()) {
            Map<String, Long> clockMap = entries.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> (String) entry.getKey(),
                            entry -> (Long) entry.getValue()
                    ));
            // Populate the internal map of the VectorClock
            clock.getClocks().putAll(clockMap);
        }
        return clock;
    }
}