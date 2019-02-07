package com.soaesps.core.DataModels.device;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.security.Principal;

@Entity
@Table(name = "DEVICES_INFO")
public class DeviceInfo implements Principal {
    @Column(name = "device_uuid", nullable = false)
    private String deviceUUID;

    @Column(name="device_type")
    @Size(min = 8, max = 40)
    private String deviceType;

    @Column(name="device_soft_model")
    @Size(min = 8, max = 100)
    private String deviceSoftModel;

    @Column(name="device_key_hash", length = 500)
    private String deviceKeyHash;

    transient private int hashCode = -1;

    protected DeviceInfo() {}

    public DeviceInfo(final String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    @Nonnull
    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(@Nonnull String deviceUUID) {
        this.deviceUUID = deviceUUID;
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

    @Override
    public int hashCode() {
        if(hashCode == -1) {
            if(deviceKeyHash != null)
                hashCode = ((deviceUUID.hashCode()+1)*(deviceKeyHash.hashCode()+1))/(deviceUUID.hashCode()+deviceKeyHash.hashCode());
            else
                hashCode = deviceUUID.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object another) {
        if(this == another) return true;
        if(another == null || !(another instanceof DeviceInfo)) {
            return false;
        }
        DeviceInfo other = (DeviceInfo) another;
        if(this.deviceUUID == null || other.getDeviceUUID() == null || !this.deviceUUID.equalsIgnoreCase(other.getDeviceUUID())) return false;
        return true;
    }

    public String getName() {
        return this.deviceUUID;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("DeviceInfo [uuid=");
        builder.append(this.deviceUUID);
        builder.append(", type=");
        builder.append(this.deviceType);
        builder.append(", softModel=");
        builder.append(this.deviceSoftModel);
        builder.append(", keyHash=");
        builder.append(this.deviceKeyHash);
        builder.append("]");
        return builder.toString();
    }
}