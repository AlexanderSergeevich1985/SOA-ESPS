package com.soaesps.aggregator.domain;

public record DeviceStats(
        String deviceId, Long userId, String metricName,
        double avg, double min, double max, double stddev, double maxAnomaly) {}