package com.soaesps.core.DataModels.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;
import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.hibernate.annotations.BatchSize;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name="USER_PROFILES")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserProfile extends BaseEntity {
    @Column(name = "login", nullable = false)
    @Size(min = 8, max = 100)
    private String userName;

    @OneToOne(mappedBy = UserInfo.USER_PROFILE_PROPERTY, cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private UserInfo userInfo;

    @OneToMany(mappedBy = DeviceInfo.USER_PROFILE_PROPERTY, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 10)
    private Set<DeviceInfo> devices;

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

    public Set<DeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(Set<DeviceInfo> devices) {
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
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        // Handle Hibernate proxies correctly using fields/getters validation
        if (!(obj instanceof UserProfile)) return false;
        UserProfile other = (UserProfile) obj;

        if (this.userName == null || other.getUserName() == null) {
            return false;
        }
        return this.userName.equalsIgnoreCase(other.getUserName());
    }
}