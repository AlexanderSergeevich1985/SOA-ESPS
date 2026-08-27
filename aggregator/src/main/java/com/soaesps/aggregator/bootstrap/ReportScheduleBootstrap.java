package com.soaesps.aggregator.bootstrap;

import com.soaesps.aggregator.workflow.ReportFanOutWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportScheduleBootstrap {

    private final WorkflowClient client;

    /** Idempotent: if the cron workflow already runs, the start is a no-op. */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureCronStarted() {
        ReportFanOutWorkflow wf = client.newWorkflowStub(
                ReportFanOutWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("report-fanout-cron")
                        .setTaskQueue("report-queue")
                        .setCronSchedule("0 */6 * * *")   // every 6 hours
                        .build());
        try {
            WorkflowClient.start(wf::generateAll, 6);
        } catch (WorkflowExecutionAlreadyStarted e) {
            // already scheduled from a previous boot — fine
        }
    }
}