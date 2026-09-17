package com.soaesps.aggregator.domain;

/**
 * Lightweight reference to a device, passed across activities,
 * workflows and repositories instead of the full event payload.
 */
public record DeviceRef(String deviceId, long userId) {

    public static DeviceRef of(MlMetricEvent event) {
        return new DeviceRef(event.deviceId(), event.userId());
    }
}