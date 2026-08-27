package com.soaesps.aggregator.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DeviceReportWorkflow {
    @WorkflowMethod
    void run(DeviceRef device, int windowHours);
}