package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.FieldUpdaterI;
import com.soaesps.core.stateflow.ObjStateDiff;

import java.util.Set;

public class GPSPositionDiff extends ObjStateDiff<GPSPosition> {
    @Override
    public GPSPosition getObject() {
        GPSPosition updated = super.getObject();
        if (updated == null) {
            updated = new GPSPosition() {
                @Override
                public void setLongitude(final Double longitude) {
                    super.setLongitude(longitude);
                    getUpdates().put("longitude", null);
                }

                @Override
                public void setLatitude(final Double latitude) {
                    super.setLatitude(latitude);
                    getUpdates().put("latitude", null);
                }

                @Override
                public void setAltitude(final Double altitude) {
                    super.setAltitude(altitude);
                    getUpdates().put("altitude", null);
                }
            };
            super.setObject(updated);
        }

        return updated;
    }

    public FieldUpdaterI<GPSPosition> getUpdater(Class clazz) {
        return new FieldUpdaterI<GPSPosition>() {
            @Override
            public GPSPosition update(GPSPosition obj) {
                Set<String> fields = getUpdatedFields();
                if (fields != null && !fields.isEmpty()) {
                    GPSPosition updated = getObject();
                    if (!fields.contains("longitude")) {
                        updated.setLongitude(obj.getLongitude());
                    }
                    if (!fields.contains("latitude")) {
                        updated.setLatitude(obj.getLatitude());
                    }
                    if (!fields.contains("altitude")) {
                        updated.setAltitude(obj.getAltitude());
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
        GPSPosition updated = this.getObject();
        switch (key) {
            case "longitude":
                updated.setLongitude((double) value);
                break;
            case "latitude":
                updated.setLatitude((double) value);
                break;
            case "altitude":
                updated.setAltitude((double) value);
                break;
            default:
                break;
        }
    }
}