package com.soaesps.notifications.dto;

import java.util.UUID;

public class BranchStatus {
    private final UUID userId;
    private final String channelName;
    private final String resultStatus;

    public BranchStatus(UUID userId, String channelName, String resultStatus) {
        this.userId = userId;
        this.channelName = channelName;
        this.resultStatus = resultStatus;
    }

    public UUID getUserId() { return userId; }
    public String getChannelName() { return channelName; }
    public String getResultStatus() { return resultStatus; }
}