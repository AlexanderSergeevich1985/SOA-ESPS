package com.soaesps.jetdag.workflow;

import com.soaesps.jetdag.dto.PipelineDto;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DynamicDagWorkflow {

    /**
     * Main orchestration entry point triggered by an LLM agent or human.
     */
    @WorkflowMethod
    void executeDag(PipelineDto dagConfig);
}