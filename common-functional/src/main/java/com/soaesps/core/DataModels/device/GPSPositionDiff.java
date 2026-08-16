package com.soaesps.core.DataModels.device;

import com.soaesps.core.stateflow.FieldUpdaterI;
import com.soaesps.core.stateflow.ObjStateDiff;
import com.soaesps.core.stateflow.UpdateI;
import jakarta.annotation.Nonnull;

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
    public <U extends UpdateI> void setUpdate(@Nonnull final String key, final U value) {
        GPSPosition updated = this.getObject();
        if (updated == null || value == null) {
            return;
        }

        // We delegate the generic type wrapper extraction or do safe type verification
        Object rawValue = value;

        switch (key) {
            case "longitude":
                if (rawValue instanceof Double) {
                    updated.setLongitude((Double) rawValue);
                } else if (rawValue instanceof Number) {
                    updated.setLongitude(((Number) rawValue).doubleValue());
                }
                break;
            case "latitude":
                if (rawValue instanceof Double) {
                    updated.setLatitude((Double) rawValue);
                } else if (rawValue instanceof Number) {
                    updated.setLatitude(((Number) rawValue).doubleValue());
                }
                break;
            case "altitude":
                if (rawValue instanceof Double) {
                    updated.setAltitude((Double) rawValue);
                } else if (rawValue instanceof Number) {
                    updated.setAltitude(((Number) rawValue).doubleValue());
                }
                break;
            default:
                // Fallback to push changes into the standard tracking map of the parent class
                super.setUpdate(key, value);
                break;
        }
    }
}