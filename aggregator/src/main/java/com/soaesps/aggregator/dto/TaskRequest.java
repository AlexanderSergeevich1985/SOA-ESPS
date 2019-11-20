package com.soaesps.aggregator.dto;

import com.soaesps.core.DataModels.executor.Payload;

public class TaskRequest implements Payload {
    private Long taskId;

    private String jobKey;

    private String workerNodeId;

    private String reservedNodeId;

    public TaskRequest(final Long taskId, final String jobKey) {
        this.taskId = taskId;
        this.jobKey = jobKey;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(final Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(final String jobKey) {
        this.jobKey = jobKey;
    }

    public String getWorkerNodeId() {
        return workerNodeId;
    }

    @Override
    public void setWorkerNodeId(final String workerNodeId) {
        this.workerNodeId = workerNodeId;
    }

    public String getReservedNodeId() {
        return reservedNodeId;
    }

    @Override
    public void setReservedNodeId(final String reservedNodeId) {
        this.reservedNodeId = reservedNodeId;
    }
}