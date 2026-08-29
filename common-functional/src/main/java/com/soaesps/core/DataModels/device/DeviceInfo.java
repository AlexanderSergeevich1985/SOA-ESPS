package com.soaesps.core.DataModels.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

@Entity
@Table(name = "DEVICES_INFO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeviceInfo extends BaseEntity implements Principal, Serializable {

    @Column(name = "device_uuid", nullable = false, unique = true)
    private String deviceUUID;

    @Column(name = "device_type")
    @Size(min = 8, max = 40)
    private String deviceType;

    @Column(name = "device_soft_model")
    @Size(min = 8, max = 100)
    private String deviceSoftModel;

    @Column(name = "device_key_hash", length = 500)
    private String deviceKeyHash;

    protected DeviceInfo() {}

    public DeviceInfo(@Nonnull String deviceUUID) {
        this.deviceUUID = Objects.requireNonNull(deviceUUID, "deviceUUID must not be null");
    }

    @Nonnull
    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(@Nonnull String deviceUUID) {
        this.deviceUUID = Objects.requireNonNull(deviceUUID);
    }

    @Nullable
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(@Nullable String deviceType) {
        this.deviceType = deviceType;
    }

    @Nullable
    public String getDeviceSoftModel() {
        return deviceSoftModel;
    }

    public void setDeviceSoftModel(@Nullable String deviceSoftModel) {
        this.deviceSoftModel = deviceSoftModel;
    }

    @Nullable
    public String getDeviceKeyHash() {
        return deviceKeyHash;
    }

    public void setDeviceKeyHash(@Nullable String deviceKeyHash) {
        this.deviceKeyHash = deviceKeyHash;
    }

    /**
     * Business key: deviceUUID uniquely identifies the device.
     * Consistent with equals() — both use only deviceUUID.
     */
    @Override
    public int hashCode() {
        return deviceUUID != null ? deviceUUID.toLowerCase().hashCode() : 0;
    }

    /**
     * Two DeviceInfo are equal iff they have the same deviceUUID (case-insensitive).
     * deviceKeyHash may change (key rotation) without changing identity.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DeviceInfo other = (DeviceInfo) obj;
        if (this.deviceUUID == null || other.deviceUUID == null) return false;
        return this.deviceUUID.equalsIgnoreCase(other.deviceUUID);
    }

    @Override
    public String getName() {
        return deviceUUID;
    }

    @Override
    public String toString() {
        return "DeviceInfo{uuid=" + deviceUUID +
                ", type=" + deviceType +
                ", softModel=" + deviceSoftModel +
                ", keyHash=" + (deviceKeyHash != null ? "***" : "null") + '}';
    }
}