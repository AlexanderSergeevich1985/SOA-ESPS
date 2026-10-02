package com.soaesps.jetdag.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record PipelineDto(
        UUID id,
        String name,
        String cronSchedule,
        List<TaskDto> tasks,
        List<TaskDependencyDto> dependencies
) {
    // Utility method to build a quick lookup map of tasks by their unique code
    public Map<String, TaskDto> getTasksMap() {
        return tasks.stream()
                .collect(Collectors.toMap(TaskDto::taskCode, task -> task));
    }

    // Computes inverted graph representation: TaskCode -> List of its Parent TaskCodes (Upstream)
    public Map<String, List<String>> getUpstreamDependencies() {
        Map<UUID, String> idToCodeMap = tasks.stream()
                .collect(Collectors.toMap(TaskDto::id, TaskDto::taskCode));

        return dependencies.stream()
                .collect(Collectors.groupingBy(
                        dep -> idToCodeMap.get(dep.downstreamTaskId()),
                        Collectors.mapping(dep -> idToCodeMap.get(dep.upstreamTaskId()), Collectors.toList())
                ));
    }

    // Identifies terminal tasks (leaves) that have no subsequent downstream jobs
    public boolean isTerminalTask(UUID taskId) {
        return dependencies.stream()
                .noneMatch(dep -> dep.upstreamTaskId().equals(taskId));
    }
}