package com.soaesps.notifications.domain;

public enum MessageStatus {
    QUEUED,
    HANDLED,
    PROCESSED,
    SENT_STARTED,
    SENT_FINISHED,
    SKIPPED,
    ERROR
}