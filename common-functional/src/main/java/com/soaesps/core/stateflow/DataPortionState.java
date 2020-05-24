package com.soaesps.core.stateflow;

import java.time.LocalDateTime;
import java.util.List;

public class DataPortionState {
    private LocalDateTime lastGlobalFixedStateTime;

    private State lastGlobalFixedState;

    private List<PortionStateTransition> batchOfUpdates;

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

    public List<PortionStateTransition> getBatchOfUpdates() {
        return batchOfUpdates;
    }

    public void setBatchOfUpdates(final List<PortionStateTransition> batchOfUpdates) {
        this.batchOfUpdates = batchOfUpdates;
    }
}
