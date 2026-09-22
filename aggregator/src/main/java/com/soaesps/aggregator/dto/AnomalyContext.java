package com.soaesps.aggregator.dto;

public record AnomalyContext(
        String deviceId,
        String deviceModel,
        String metricName,
        double value,
        double anomalyScore
) {}