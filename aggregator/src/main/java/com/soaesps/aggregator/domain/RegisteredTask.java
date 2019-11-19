package com.soaesps.aggregator.domain;

import com.soaesps.core.DataModels.executor.Payload;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@RedisHash("registered_task")
public class RegisteredTask implements Payload {
    @Id
    @javax.persistence.Id
    private Long taskId;

    @NotBlank
    @Size(min = 8, max = 50)
    private String workerNodeId;

    @NotBlank
    @Size(min = 8, max = 50)
    private String reservedNodeId;

    private Instant startTime;

    private Instant endTime;

    @Transient
    private JobDesc jobDesc;

    public RegisteredTask() {}

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

    @NotBlank
    public String getReservedNodeId() {
        return reservedNodeId;
    }

    public void setReservedNodeId(@NotBlank final String reservedNodeId) {
        this.reservedNodeId = reservedNodeId;
    }

    @Nullable
    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(@Nullable final Instant startTime) {
        this.startTime = startTime;
    }

    public JobDesc getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(@NotNull final JobDesc jobDesc) {
        this.jobDesc = jobDesc;
    }

    @Override
    public String getJobKey() {
        return jobDesc.getJobKey();
    }
}