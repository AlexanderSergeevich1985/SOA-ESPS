package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.ObjState;

public class DeviceState extends ObjState<DeviceDesc, DeviceDescDiff> {
    public DeviceState(DeviceState prev, DeviceDescDiff diff) {
        super(prev, diff);
    }

    @Override
    protected DeviceDesc calcUpdatedState(DeviceDesc state, DeviceDescDiff stateDiff) {
        return stateDiff.update(state);
    }
}