package com.soaesps.aggregator.activity;

import com.soaesps.aggregator.domain.DeviceStats;
import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface ReportActivities {

    List<DeviceStats> fetchStats(String deviceId, int windowHours);

    /** Flaky by nature: LLM call. Retries are configured on the workflow side. */
    String askLlm(DeviceRef device, List<DeviceStats> stats);

    void publishAdvice(Long userId, String deviceId, String advice, String severity);

    List<DeviceRef> listActiveDevices();
}