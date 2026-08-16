package com.soaesps.aggregator.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class CompletionTask {
    @Id
    private Long taskId;

    @NotBlank
    @Size(min = 8, max = 50)
    private String workerNodeId;

    private Instant endTime;

    @NotNull
    public Long getTaskId() {
        return this.taskId;
    }

    public void setTaskId(@NotNull Long taskId) {
        this.taskId = taskId;
    }

    @NotBlank
    public String getWorkerNodeId() {
        return workerNodeId;
    }

    public void setWorkerNodeId(@NotBlank final String workerNodeId) {
        this.workerNodeId = workerNodeId;
    }

    @Nullable
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(@Nullable final Instant endTime) {
        this.endTime = endTime;
    }
}