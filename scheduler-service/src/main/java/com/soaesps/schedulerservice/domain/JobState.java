package com.soaesps.schedulerservice.domain;

public enum JobState {
    PLANNED("planned"),
    STARTED("started"),
    INPROCESSING("inprocessing"),
    EXECUTED("executed"),
    FAILED("failed");

    private String state;

    JobState(final String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}