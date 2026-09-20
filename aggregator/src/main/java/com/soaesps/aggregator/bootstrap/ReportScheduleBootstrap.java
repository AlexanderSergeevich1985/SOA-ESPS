package com.soaesps.aggregator.bootstrap;

import com.soaesps.aggregator.workflow.ReportFanOutWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Robust background bootstrap interceptor.
 * Guarantees Spring context safety against cold-start container orchestration latency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduleBootstrap {

    private final WorkflowClient client;

    /**
     * Spawns a dedicated low-priority background thread to register the Temporal cron schedule.
     * Prevents gRPC connection refused crashes from killing the primary Spring application context.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureCronStarted() {
        // Enforce native thread isolation instead of relying on default shared pools
        Thread bootstrapThread = new Thread(() -> {
            log.info("[Temporal Bootstrap] Dedicated background scheduling routine activated.");

            int maxAttempts = 15;
            int attempt = 0;
            boolean isSuccessful = false;

            while (!isSuccessful && attempt < maxAttempts) {
                try {
                    attempt++;
                    log.info("[Temporal Bootstrap] Probing orchestrator socket channel... (Attempt {}/{})", attempt, maxAttempts);

                    ReportFanOutWorkflow workflowStub = client.newWorkflowStub(
                            ReportFanOutWorkflow.class,
                            WorkflowOptions.newBuilder()
                                    .setWorkflowId("report-fanout-cron")
                                    .setTaskQueue("report-queue")
                                    .setCronSchedule("0 */6 * * *") // Every 6 hours standard interval
                                    .build());

                    // Execute the invocation proxy request on the remote cluster
                    WorkflowClient.start(workflowStub::generateAll, 6);

                    log.info("[Temporal Bootstrap] Singleton workflow 'report-fanout-cron' registered successfully.");
                    isSuccessful = true;

                } catch (WorkflowExecutionAlreadyStarted e) {
                    log.info("[Temporal Bootstrap] Target schedule state verified: 'report-fanout-cron' is already active on the server.");
                    isSuccessful = true;
                } catch (Exception ex) {
                    log.warn("[Temporal Bootstrap] Socket target closed or unavailable: {}. Retrying in 5 seconds...", ex.getMessage());
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("[Temporal Bootstrap] Background routine thread interrupted abruptly.", e);
                        break;
                    }
                }
            }

            if (!isSuccessful) {
                log.error("[Temporal Bootstrap] CRITICAL: Engine connection bounds exceeded. Cron schedule omitted.");
            }
        }, "temporal-cron-bootstrap-thread");

        // Start execution instantly - this yields back the main Spring boot threads loop
        bootstrapThread.setDaemon(true);
        bootstrapThread.start();
    }
}