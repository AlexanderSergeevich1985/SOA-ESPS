package com.soaesps.aggregator.domain;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;

public class RegisteredTask {
    @NotBlank
    @Size(min = 8, max = 50)
    private String workerNodeId;

    @NotBlank
    @Size(min = 8, max = 50)
    private String reservedNodeId;

    private Instant startTime;

    private Instant endTime;

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

    @Nullable
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(@Nullable final Instant endTime) {
        this.endTime = endTime;
    }
}