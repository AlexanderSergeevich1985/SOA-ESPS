package com.soaesps.notifications.dto;

public class BranchStatus {
    private final Long userId;
    private final String channelName;
    private final String resultStatus;

    public BranchStatus(Long userId, String channelName, String resultStatus) {
        this.userId = userId;
        this.channelName = channelName;
        this.resultStatus = resultStatus;
    }

    public Long getUserId() { return userId; }
    public String getChannelName() { return channelName; }
    public String getResultStatus() { return resultStatus; }
}