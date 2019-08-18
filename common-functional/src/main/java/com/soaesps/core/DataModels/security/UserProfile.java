package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;
import com.soaesps.core.DataModels.device.DeviceInfo;
import org.hibernate.annotations.BatchSize;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name="USER_PROFILES")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserProfile extends BaseEntity {
    @Column(name = "login", nullable = false)
    @Size(min = 8, max = 100)
    private String userName;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = UserInfo.USER_PROFILE_PROPERTY, optional = false)
    private UserInfo userInfo;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @JoinTable(
            name = "USERS_DEVICE",
            joinColumns = { @JoinColumn(name = "user_profile_id") },
            inverseJoinColumns = { @JoinColumn(name = "device_id") }
    )
    @BatchSize(size = 10)
    private List<DeviceInfo> devices;

    @Transient
    private BaseUserDetails userDetails;

    public UserProfile() {}

    @NotNull
    public String getUserName() {
        return userName;
    }

    public void setUserName(@NotNull String userName) {
        this.userName = userName;
    }

    @Nullable
    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public void setUserInfo(@Nullable UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<DeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }

    @Nullable
    public BaseUserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(@Nullable BaseUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public int hashCode() {
        return this.getId().intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || !(obj instanceof UserProfile)) {
            return false;
        }
        UserProfile other = (UserProfile) obj;
        if(this.userName == null || other.getUserName() == null || !this.userName.equalsIgnoreCase(other.getUserName())) return false;
        return true;
    }
}