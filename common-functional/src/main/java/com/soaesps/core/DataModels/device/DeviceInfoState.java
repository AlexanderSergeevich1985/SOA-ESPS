package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.ObjState;

public class DeviceInfoState extends ObjState<DeviceInfo, DeviceInfoDiff> {
    public DeviceInfoState(DeviceInfoState prevStateRef, DeviceInfoDiff stateDiff) {
        super(prevStateRef, stateDiff);
    }

    @Override
    protected DeviceInfo calcUpdatedState(DeviceInfo state, DeviceInfoDiff stateDiff) {
        return stateDiff.update(state);
    }
}