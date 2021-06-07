package com.soaesps.core.DataModels.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "DEVICES_DESC")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeviceDesc implements Serializable {
    private DeviceInfo info;

    private GPSPosition position;

    public DeviceInfo getInfo() {
        return info;
    }

    public void setInfo(DeviceInfo info) {
        this.info = info;
    }

    public GPSPosition getPosition() {
        return position;
    }

    public void setPosition(GPSPosition position) {
        this.position = position;
    }
}