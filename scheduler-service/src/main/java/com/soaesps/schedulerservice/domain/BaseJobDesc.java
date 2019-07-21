package com.soaesps.schedulerservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soaesps.core.Utils.convertor.hibernate.TimestampConverter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Embeddable
public class BaseJobDesc {
    @JsonProperty("start_time")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @JsonProperty("exception_time")
    @Convert(converter = TimestampConverter.class)
    @Column(name = "exception_time")
    private ZonedDateTime endTime;

    @JsonProperty("exception")
    @Column(name = "exception")
    private Exception exception;

    @JsonProperty("additional_info")
    @Column(name = "additional_info")
    private String additionalInfo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduler_task")
    private SchedulerTask task;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}