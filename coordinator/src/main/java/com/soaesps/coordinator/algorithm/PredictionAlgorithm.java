package com.soaesps.coordinator.algorithm;

/**
 * Abstraction for network latency prediction algorithms.
 * Allows swapping implementations (e.g., Particle Filter, Exponential Smoothing)
 * without modifying the RaftLeaderCoordinator logic.
 */
public interface PredictionAlgorithm {

    /**
     * Ingests a new Round-Trip Time observation.
     * Implementations must be thread-safe and non-blocking.
     *
     * @param rttMs The measured round-trip time in milliseconds.
     */
    void observeRtt(double rttMs);

    /**
     * Retrieves the current safe prediction for timeout configuration.
     * Typically returns a value like mean + 2*sigma to account for jitter.
     *
     * @return The predicted safe timeout window in milliseconds.
     */
    double getPrediction();
}