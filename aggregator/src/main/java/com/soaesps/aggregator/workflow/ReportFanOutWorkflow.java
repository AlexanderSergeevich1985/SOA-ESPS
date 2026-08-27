package com.soaesps.aggregator.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ReportFanOutWorkflow {
    @WorkflowMethod
    void generateAll(int windowHours);
}