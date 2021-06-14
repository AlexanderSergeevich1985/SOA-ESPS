package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.ObjState;

public class GPSPositionState extends ObjState<GPSPosition, GPSPositionDiff> {
    public GPSPositionState(GPSPositionState prevStateRef, GPSPositionDiff stateDiff) {
        super(prevStateRef, stateDiff);
    }

    @Override
    protected GPSPosition calcUpdatedState(GPSPosition state, GPSPositionDiff stateDiff) {
        return stateDiff.update(state);
    }
}