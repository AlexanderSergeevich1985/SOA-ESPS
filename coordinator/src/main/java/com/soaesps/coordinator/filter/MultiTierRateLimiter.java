package com.soaesps.coordinator.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-tier rate limiter supporting different limits for different user tiers.
 * Uses Bucket4j 8.x builder API for token bucket algorithm implementation.
 */
public class MultiTierRateLimiter {

    // Default limits: 100 requests per minute for regular users
    private static final int DEFAULT_LIMIT = 100;
    private static final Duration DEFAULT_REFILL_DURATION = Duration.ofMinutes(1);

    // Premium limits: 1000 requests per minute
    private static final int PREMIUM_LIMIT = 1000;
    private static final Duration PREMIUM_REFILL_DURATION = Duration.ofMinutes(1);

    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    /**
     * Gets or creates a rate limit bucket for the given user.
     *
     * @param userId    The user identifier.
     * @param isPremium Whether the user has premium status.
     * @return The rate limit bucket for the user.
     */
    public Bucket getBucket(String userId, boolean isPremium) {
        return userBuckets.computeIfAbsent(userId, k -> createBucket(isPremium));
    }

    /**
     * Gets or creates a rate limit bucket for the given user with default (non-premium) limits.
     *
     * @param userId The user identifier.
     * @return The rate limit bucket for the user.
     */
    public Bucket getBucket(String userId) {
        return getBucket(userId, false);
    }

    /**
     * FIX: Use new Bucket4j 8.x builder API instead of deprecated Bandwidth.classic() + Refill.intervally()
     */
    private Bucket createBucket(boolean isPremium) {
        int limit = isPremium ? PREMIUM_LIMIT : DEFAULT_LIMIT;
        Duration refillDuration = isPremium ? PREMIUM_REFILL_DURATION : DEFAULT_REFILL_DURATION;

        // New builder API (replaces deprecated Bandwidth.classic + Refill.intervally)
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillIntervally(limit, refillDuration)
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Removes a user's bucket (e.g., on logout or account deletion).
     *
     * @param userId The user identifier.
     */
    public void removeBucket(String userId) {
        userBuckets.remove(userId);
    }

    /**
     * Returns the current number of tracked user buckets.
     * Useful for monitoring and cleanup.
     *
     * @return The number of active buckets.
     */
    public int getActiveBucketCount() {
        return userBuckets.size();
    }

    /**
     * Clears all buckets (e.g., for testing or system reset).
     */
    public void clearAllBuckets() {
        userBuckets.clear();
    }
}