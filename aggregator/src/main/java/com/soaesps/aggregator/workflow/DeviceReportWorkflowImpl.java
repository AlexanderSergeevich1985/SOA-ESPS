package com.soaesps.aggregator.workflow;

import com.soaesps.aggregator.activity.ReportActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.retry.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(taskQueues = "report-queue")
public class DeviceReportWorkflowImpl implements DeviceReportWorkflow {

    // DETERMINISM RULE: no DB / clock / random / logs via SLF4J here.
    // Only activities, Workflow.sleep, Workflow.currentTimeMillis, Workflow.getLogger.

    @Override
    public void run(DeviceRef device, int windowHours) {
        ReportActivities quick = Workflow.newActivityStub(ReportActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(30))
                        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                        .build());

        // LLM activity: generous timeouts + exponential backoff — Temporal retries
        // across worker restarts, @Scheduled would just die here
        ReportActivities llm = Workflow.newActivityStub(ReportActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(120))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(5)
                                .setInitialInterval(Duration.ofSeconds(2))
                                .setBackoffCoefficient(2.0)
                                .build())
                        .build());

        List<DeviceStats> stats = quick.fetchStats(device.deviceId(), windowHours);
        if (stats.isEmpty()) {
            return;   // device was silent — nothing to report
        }

        String advice = llm.askLlm(device, stats);
        double worst = stats.stream().mapToDouble(DeviceStats::maxAnomaly).max().orElse(0);
        quick.publishAdvice(device.userId(), device.deviceId(), advice,
                worst > 0.8 ? "high" : worst > 0.5 ? "medium" : "low");
    }
}