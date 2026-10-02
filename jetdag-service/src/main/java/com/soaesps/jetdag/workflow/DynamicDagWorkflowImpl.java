package com.soaesps.jetdag.workflow;

import com.soaesps.jetdag.activity.HttpTaskActivities;
import com.soaesps.jetdag.dto.PipelineDto;
import com.soaesps.jetdag.dto.TaskDto;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.*;

public class DynamicDagWorkflowImpl implements DynamicDagWorkflow {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void executeDag(PipelineDto dagConfig) {
        Map<String, TaskDto> tasksMap = dagConfig.getTasksMap();

        // Find the root tasks (tasks that don't have any upstream parents) to start execution
        List<TaskDto> rootTasks = findRootTasks(dagConfig);

        // Track completed tasks to prevent cyclic/duplicate executions in complex meshes
        Set<String> completedTasks = new HashSet<>();

        for (TaskDto rootTask : rootTasks) {
            executeTaskWithFeedback(rootTask, tasksMap, dagConfig, completedTasks);
        }
    }

    private void executeTaskWithFeedback(TaskDto task, Map<String, TaskDto> tasksMap,
                                         PipelineDto dagConfig, Set<String> completedTasks) {

        if (completedTasks.contains(task.taskCode())) return;

        HttpTaskActivities activities = Workflow.newActivityStub(
                HttpTaskActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(task.timeoutSeconds()))
                        .build()
        );

        boolean isSuccess = false;
        try {
            var jsonNode = objectMapper.readTree(task.paramsJson());
            String url = jsonNode.get("url").asText();
            String method = jsonNode.get("method").asText();
            String payload = jsonNode.has("payload") ? jsonNode.get("payload").toString() : "{}";

            // 1. Fire the HTTP call and wait for feedback (200 OK)
            activities.executeHttpCall(url, method, payload);
            isSuccess = true;

            Workflow.getLogger(DynamicDagWorkflowImpl.class)
                    .info("Task [{}] completed with SUCCESS feedback.", task.taskCode());

        } catch (Exception e) {
            Workflow.getLogger(DynamicDagWorkflowImpl.class)
                    .error("Task [{}] received FAILURE feedback: {}", task.taskCode(), e.getMessage());
        }

        completedTasks.add(task.taskCode());

        // 2. Decision Tree based on feedback
        if (isSuccess) {
            // Move forward: Find all downstream tasks that depend on this one
            List<TaskDto> nextTasks = findDownstreamTasks(task.id(), dagConfig, tasksMap);
            for (TaskDto next : nextTasks) {
                // In a production app, you'd check if ALL parents of 'next' are completed
                executeTaskWithFeedback(next, tasksMap, dagConfig, completedTasks);
            }
        } else {
            // Trigger Fallback process if configured in PostgreSQL
            if (task.fallbackTaskCode() != null && tasksMap.containsKey(task.fallbackTaskCode())) {
                TaskDto fallbackTask = tasksMap.get(task.fallbackTaskCode());
                Workflow.getLogger(DynamicDagWorkflowImpl.class)
                        .warn("Executing fallback process [{}] for failed task [{}]", fallbackTask.taskCode(), task.taskCode());

                executeTaskWithFeedback(fallbackTask, tasksMap, dagConfig, completedTasks);
            } else {
                throw new RuntimeException("Task failed and no fallback was specified for: " + task.taskCode());
            }
        }
    }

    private List<TaskDto> findRootTasks(PipelineDto graph) {
        // Logic to return tasks that have no upstream dependencies
        return graph.tasks().stream()
                .filter(t -> graph.getUpstreamDependencies().getOrDefault(t.taskCode(), List.of()).isEmpty())
                .toList();
    }

    private List<TaskDto> findDownstreamTasks(UUID taskId, PipelineDto graph, Map<String, TaskDto> tasksMap) {
        return graph.dependencies().stream()
                .filter(dep -> dep.upstreamTaskId().equals(taskId))
                .map(dep -> graph.tasks().stream().filter(t -> t.id().equals(dep.downstreamTaskId())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}