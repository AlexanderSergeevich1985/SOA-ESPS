package com.soaesps.schedulerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public abstract class SysEventDTO {
    @JsonProperty("start_time")
    private ZonedDateTime startTimeStamp;

    @JsonProperty("end_time")
    private ZonedDateTime endTimeStamp;

    public ZonedDateTime getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(ZonedDateTime startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public ZonedDateTime getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(ZonedDateTime endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }
}