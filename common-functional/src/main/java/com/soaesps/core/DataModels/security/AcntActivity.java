package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ACCOUNT_ACTIVITY")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AcntActivity extends BaseEntity implements Serializable {

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @Column(name = "action_type", length = 100, nullable = false)
    private String actionType; // e.g., "LOGIN_SUCCESS", "PASSWORD_CHANGE", "PAYMENT_INITIATED"

    @Column(name = "activity_description", length = 1000)
    private String description;

    @Column(name = "ip_address", length = 45) // Length 45 covers both IPv4 and IPv6 formats
    private String ipAddress;

    @Column(name = "user_agent", length = 500) // Stores browser or client application details
    private String userAgent;

    @Column(name = "status", length = 50, nullable = false)
    private String status; // e.g., "SUCCESS", "FAILED", "BLOCKED"

    @Column(name = "error_message", length = 1000) // Stores failure details if status is FAILED
    private String errorMessage;

    public AcntActivity() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AcntActivity that = (AcntActivity) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(actionType, that.actionType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(status, that.status) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, actionType, description, ipAddress, userAgent, status, errorMessage);
    }
}