package com.soaesps.jetdag.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.ActivityCompletionClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newLocalServiceStubs(); // Connects to localhost:7233 by default
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs);
    }

    /**
     * Required for asynchronous reactive activity completion.
     */
    @Bean
    public ActivityCompletionClient activityCompletionClient(WorkflowClient workflowClient) {
        return workflowClient.newActivityCompletionClient();
    }
}