package com.soaesps.aggregator.domain;

/**
 * Kafka topic names shared by all aggregator pipelines.
 */
public final class Topics {

    /** Raw telemetry enriched by msg-process (input). */
    public static final String ML_METRICS = "ml-metrics";

    /** Human-readable advice consumed by notifications-service (output). */
    public static final String USER_ADVICE = "user-advice";

    private Topics() {
    }
}