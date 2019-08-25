package com.soaesps.core.DataModels.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soaesps.core.Utils.convertor.hibernate.TimestampConverter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Embeddable
public class BaseJobDesc {
    @JsonProperty("job_key")
    @Column(name = "job_key")
    private String jobKey;

    @JsonProperty("start_time")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @JsonProperty("exception_time")
    @Convert(converter = TimestampConverter.class)
    @Column(name = "exception_time")
    private LocalDateTime endTime;

    @JsonProperty("exception")
    @Column(name = "exception")
    private Exception exception;

    @JsonProperty("additional_info")
    @Column(name = "additional_info")
    private String additionalInfo;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(final Exception exception) {
        this.exception = exception;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}