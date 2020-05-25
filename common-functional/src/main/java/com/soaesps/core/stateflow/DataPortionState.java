package com.soaesps.core.stateflow;

import com.soaesps.core.Utils.DataStructure.QueueI;

import java.time.LocalDateTime;
import java.util.Set;

public class DataPortionState {
    private LocalDateTime lastGlobalFixedStateTime;

    private State lastGlobalFixedState;

    private Set<PortionStateTransition> batchOfUpdates;

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