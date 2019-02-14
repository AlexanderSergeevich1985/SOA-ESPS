package com.soaesps.aggregator.domain;

import javax.annotation.Nullable;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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