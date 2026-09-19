package com.soaesps.aggregator.config;

import com.soaesps.aggregator.activity.ReportActivities;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Missing Infrastructure Configuration for the Temporal.io Client and Worker execution engine.
 * Connects to the Temporal Docker orchestrator container and binds local activities.
 */
@Configuration
public class TemporalConfig {

    @Value("${temporal.connection.target:localhost:7233}")
    private String temporalTarget;

    /**
     * Establishes the low-level gRPC network communication channel with the Temporal Server.
     */
    @Bean(destroyMethod = "shutdown")
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newInstance(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalTarget)
                        .build());
    }

    /**
     * Creates the primary client bean consumed by ReportScheduleBootstrap to trigger and monitor workflows.
     */
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs,
                WorkflowClientOptions.newBuilder().build());
    }

    /**
     * Factory that manages the lifecycle of background execution threads (Workers).
     */
    @Bean(destroyMethod = "shutdown")
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    /**
     * Starts a background Worker listening to the 'report-queue' specified in your WorkflowImpls.
     * Registers the actual bean implementation of ReportActivities so Temporal can invoke them.
     */
    //@Bean
    public Worker reportWorker(WorkerFactory factory, ReportActivities reportActivitiesImpl) {
        // Matches taskQueues = "report-queue" inside DeviceReportWorkflowImpl
        Worker worker = factory.newWorker("report-queue");

        // Register the activities implementation (database fetching, LLM generation)
        worker.registerActivitiesImplementations(reportActivitiesImpl);

        // Start polling the queue immediately in a background thread pool
        factory.start();

        return worker;
    }
}