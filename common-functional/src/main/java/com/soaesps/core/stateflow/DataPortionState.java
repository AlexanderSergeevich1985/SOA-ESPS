package com.soaesps.core.stateflow;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class DataPortionState {
    private UUID uuid;

    private final Long iteration;

    private LocalDateTime lastGlobalFixedStateTime;

    private State lastGlobalFixedState;

    private Set<PortionStateTransition> batchOfUpdates;

    public DataPortionState(final Long iteration) {
        this.iteration = iteration;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getLastGlobalFixedStateTime() {
        return lastGlobalFixedStateTime;
    }

    public void setLastGlobalFixedStateTime(final LocalDateTime lastGlobalFixedStateTime) {
        this.lastGlobalFixedStateTime = lastGlobalFixedStateTime;
    }

    public State getLastGlobalFixedState() {
        return lastGlobalFixedState;
    }

    public void setLastGlobalFixedState(State lastGlobalFixedState) {
        this.lastGlobalFixedState = lastGlobalFixedState;
    }

    public Set<PortionStateTransition> getBatchOfUpdates() {
        return batchOfUpdates;
    }

    public void setBatchOfUpdates(final Set<PortionStateTransition> batchOfUpdates) {
        this.batchOfUpdates = batchOfUpdates;
    }
}