package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.FieldUpdaterI;
import com.soaesps.core.stateflow.ObjStateDiff;
import com.soaesps.core.stateflow.UpdateI;

import java.util.Set;

public class DeviceDescDiff extends ObjStateDiff<DeviceDesc> {
    private DeviceInfoDiff infoDiff;

    private GPSPositionDiff gpsPositionDiff;

    public FieldUpdaterI<DeviceInfo> getInfoUpdater() {
        if (infoDiff == null) {
            infoDiff = new DeviceInfoDiff();
        }

        return infoDiff;
    }

    public FieldUpdaterI<GPSPosition> getGPSUpdater() {
        if (gpsPositionDiff == null) {
            gpsPositionDiff = new GPSPositionDiff();
        }

        return gpsPositionDiff;
    }

    @Override
    public DeviceDesc getObject() {
        DeviceDesc updated = super.getObject();
        if (updated == null) {
            updated = new DeviceDesc();
            super.setObject(updated);
        }

        return updated;
    }

    @Override
    public DeviceDesc update(DeviceDesc obj) {
        DeviceDesc info = this.getObject();
        FieldUpdaterI<DeviceDesc> updater = getUpdater(DeviceDesc.class);

        return updater.update(info);
    }

    @Override
    public FieldUpdaterI<DeviceDesc> getUpdater(Class clazz) {
        return new FieldUpdaterI<DeviceDesc>() {
            @Override
            public DeviceDesc update(DeviceDesc obj) {
                Set<String> fields = getUpdatedFields();
                if (fields != null && !fields.isEmpty()) {
                    DeviceDesc updated = getObject();
                    if (!fields.contains("info")) {
                        updated.setInfo(obj.getInfo());
                    } else {
                        FieldUpdaterI<DeviceInfo> infoDiff = getInfoUpdater();
                        infoDiff.update(obj.getInfo());
                    }
                    if (!fields.contains("position")) {
                        updated.setPosition(obj.getPosition());
                    } else {
                        FieldUpdaterI<GPSPosition> gpsPositionDiff = getGPSUpdater();
                        gpsPositionDiff.update(obj.getPosition());
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
    public void setUpdate(String key, UpdateI update) {
        super.getUpdates().put(key, null);
        DeviceDesc updated = this.getObject();
        switch (key) {
            case "info":
                if (infoDiff == null) {
                    infoDiff = new DeviceInfoDiff();
                }
                update.setUpdate(infoDiff.getObject());
                break;
            case "position":
                if (gpsPositionDiff == null) {
                    gpsPositionDiff = new GPSPositionDiff();
                }
                update.setUpdate(gpsPositionDiff.getObject());
                break;
            default:
                break;
        }
    }
}