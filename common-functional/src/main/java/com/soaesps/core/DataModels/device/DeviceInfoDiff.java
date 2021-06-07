package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.FieldUpdaterI;
import com.soaesps.core.stateflow.ObjStateDiff;

import java.util.Set;

public class DeviceInfoDiff extends ObjStateDiff<DeviceInfo> {
    @Override
    public DeviceInfo getObject() {
        DeviceInfo updated = super.getObject();
        if (updated == null) {
            updated = new DeviceInfo();
            super.setObject(updated);
        }

        return updated;
    }

    @Override
    public DeviceInfo update(DeviceInfo obj) {
        DeviceInfo info = this.getObject();
        FieldUpdaterI<DeviceInfo> updater = getUpdater(DeviceInfo.class);

        return updater.update(info);
    }

    public FieldUpdaterI<DeviceInfo> getUpdater(Class clazz) {
        return new FieldUpdaterI<DeviceInfo>() {
            @Override
            public DeviceInfo update(DeviceInfo obj) {
                Set<String> fields = getUpdatedFields();
                if (fields != null && !fields.isEmpty()) {
                    DeviceInfo updated = getObject();
                    if (!fields.contains("deviceUUID")) {
                        updated.setDeviceUUID(obj.getDeviceUUID());
                    }
                    if (!fields.contains("deviceType")) {
                        updated.setDeviceType(obj.getDeviceType());
                    }
                    if (!fields.contains("deviceSoftModel")) {
                        updated.setDeviceSoftModel(obj.getDeviceSoftModel());
                    }
                    if (!fields.contains("deviceKeyHash")) {
                        updated.setDeviceKeyHash(obj.getDeviceKeyHash());
                    }

                    return updated;
                }

                return obj;
            }

            @Override
            public FieldUpdaterI getUpdater(Class clazz) {
                return this;
            }
        };
    }

    @Override
    public void setUpdate(String key, Object value) {
        super.getUpdates().put(key, null);
        DeviceInfo updated = this.getObject();
        switch (key) {
            case "deviceUUID":
                updated.setDeviceUUID(value.toString());
                break;
            case "deviceType":
                updated.setDeviceType(value.toString());
                break;
            case "deviceSoftModel":
                updated.setDeviceSoftModel(value.toString());
                break;
            case "deviceKeyHash":
                updated.setDeviceKeyHash(value.toString());
                break;
            default:
                break;
        }
    }
}