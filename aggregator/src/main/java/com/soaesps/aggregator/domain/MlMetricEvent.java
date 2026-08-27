package com.soaesps.aggregator.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/** Event received from Kafka topic `ml-metrics` (produced by msg-process). */
public record MlMetricEvent(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
        String deviceId,
        long userId,
        String metricName,
        double value,
        double anomalyScore,
        String predictedState
) {}