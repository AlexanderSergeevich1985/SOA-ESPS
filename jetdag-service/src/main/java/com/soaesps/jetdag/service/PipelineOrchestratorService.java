package com.soaesps.jetdag.service;

import com.soaesps.jetdag.dto.PipelineDto;
import com.soaesps.jetdag.dto.TaskDependencyDto;
import com.soaesps.jetdag.dto.TaskDto;
import com.soaesps.jetdag.repository.TaskDependencyRepository;
import com.soaesps.jetdag.repository.TaskRepository; // Assuming this exists or will be created
import com.soaesps.jetdag.repository.PipelineRepository; // Assuming this exists or will be created
import com.soaesps.jetdag.validator.DagValidator;
import com.soaesps.jetdag.workflow.DynamicDagWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineOrchestratorService {

    private final PipelineRepository pipelineRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final WorkflowClient temporalClient;

    /**
     * Resolves the flat DB configuration tables into a unified graph architecture,
     * performs a cyclical safety check, and triggers an autonomous Temporal Workflow.
     *
     * @param pipelineId The unique ID of the pipeline to pull from PostgreSQL config.
     * @return The unique generated Workflow ID responsible for execution tracking.
     */
    public Mono<String> triggerPipeline(UUID pipelineId) {
        log.info("Starting on-demand reactive loading for pipeline metadata id: [{}]", pipelineId);

        return pipelineRepository.findById(pipelineId)
                // If the pipeline configuration doesn't exist in PostgreSQL, interrupt the stream with an error
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Pipeline configuration not found for ID: " + pipelineId)))
                .flatMap(pipeline -> {

                    // 1. Concurrently stream and map all tasks bound to this pipeline via non-blocking I/O
                    Mono<java.util.List<TaskDto>> tasksMono = taskRepository.findByPipelineId(pipelineId)
                            .map(t -> new TaskDto(
                                    t.id(),
                                    t.taskCode(),
                                    t.operatorType(),
                                    t.paramsJson(),
                                    t.fallbackTaskCode(),
                                    t.timeoutSeconds(),
                                    t.retryAttempts(),
                                    t.retryBackoffSeconds()
                            ))
                            .collectList();

                    // 2. Concurrently stream and map all topological edges (dependencies)
                    Mono<java.util.List<TaskDependencyDto>> edgesMono = dependencyRepository.findGraphEdges(pipelineId)
                            .map(e -> new TaskDependencyDto(e.upstreamTaskId(), e.downstreamTaskId()))
                            .collectList();

                    // 3. Zip reactive database streams together once both lists are fully populated in memory
                    return Mono.zip(tasksMono, edgesMono)
                            .flatMap(tuple -> {
                                java.util.List<TaskDto> tasks = tuple.getT1();
                                java.util.List<TaskDependencyDto> edges = tuple.getT2();

                                // Instantiate our immutable data-mesh map model
                                PipelineDto pipelineDto = new PipelineDto(
                                        pipeline.id(),
                                        pipeline.name(),
                                        pipeline.cronSchedule(),
                                        tasks,
                                        edges
                                );

                                // 4. Structural validation guard against infinite loops (Airflow-like circular dependencies)
                                try {
                                    DagValidator.validate(pipelineDto);
                                } catch (IllegalArgumentException ex) {
                                    // Interrupt execution before spinning up cloud resources on a broken graph
                                    return Mono.error(ex);
                                }

                                // 5. Generate a unique human-scannable Workflow execution tracking ID for Temporal State Store
                                String workflowId = String.format("dag-%s-%s",
                                        pipeline.name().replaceAll("\\s+", "_"),
                                        UUID.randomUUID().toString().substring(0, 8)
                                );

                                log.info("DAG graph successfully validated. Handing over execution to Temporal with ID: {}", workflowId);

                                // 6. Connect to our dynamic generic workflow interface proxy stub
                                DynamicDagWorkflow workflow = temporalClient.newWorkflowStub(
                                        DynamicDagWorkflow.class,
                                        WorkflowOptions.newBuilder()
                                                .setWorkflowId(workflowId)
                                                .setTaskQueue("jetdag-tasks-queue")
                                                .build()
                                );

                                // 7. Fire-and-forget native reactive gRPC dispatching to separate highly available Temporal cluster
                                try {
                                    WorkflowClient.start(workflow::executeDag, pipelineDto);
                                } catch (Exception ex) {
                                    return Mono.error(new RuntimeException("Failed to establish gRPC transaction handoff to Temporal cluster", ex));
                                }

                                return Mono.just(workflowId);
                            });
                });
    }
}