package com.soaesps.core.stateflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IterationSyncData<NodeKey extends Serializable> implements Serializable {
    private final Long iteration;

    private Map<NodeKey, Set<DataPortionState>> statesForIteration;

    public IterationSyncData(final Long iteration) {
        this.iteration = iteration;
        this.statesForIteration = new ConcurrentHashMap<>();
    }

    public Long getIteration() {
        return iteration;
    }

    public void addState(final NodeKey nodeKey, final DataPortionState state) {
        Set<DataPortionState> states = this.statesForIteration.get(nodeKey);
        if (states == null) {
            states = new HashSet<>();
            addStates(nodeKey, states);
        }
        states.add(state);
    }

    public void addStates(final NodeKey nodeKey, final Set<DataPortionState> states) {
        this.statesForIteration.put(nodeKey, states);
    }

    public Map<NodeKey, Set<DataPortionState>> getStatesForIteration() {
        return statesForIteration;
    }

    public void setStatesForIteration(final Map<NodeKey, Set<DataPortionState>> statesForIteration) {
        this.statesForIteration = statesForIteration;
    }
}