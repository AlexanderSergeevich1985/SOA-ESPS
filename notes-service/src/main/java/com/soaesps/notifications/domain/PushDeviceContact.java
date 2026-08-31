package com.soaesps.notifications.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a mobile or web device registered for FCM/APNS Push notifications.
 */
@Entity
@DiscriminatorValue("PUSH")
public class PushDeviceContact extends UserContact {

    @Column(name = "device_id")
    @NotBlank(message = "Device ID cannot be blank")
    private String deviceId;

    @NotBlank(message = "Push token cannot be blank")
    @Size(max = 500, message = "Push token exceeds max length of 500")
    @Column(name = "push_token", length = 500)
    private String pushToken;

    @NotBlank(message = "Device type cannot be blank")
    @Column(name = "device_type")
    private String deviceType; // e.g., IOS, ANDROID, WEB

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}