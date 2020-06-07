package com.soaesps.core.DataModels.security;

import com.soaesps.core.DataModels.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LogIn_Audit")
public class AuthAudit extends BaseEntity {
    public static final String USER_ID = "userId";

    public final static String LOGON_ID = "logonId";

    public final static String SESSION_UUID = "sessionUuid";

    public final static String IP_ADDRESS = "ipAddress";

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "logon_id")
    private Long logonId;

    @Column(name = "session_uuid", length = 50)
    private String sessionUuid;

    @Column(name = "ipAddress", length = 50)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50, nullable = false)
    private SecEvent actionType;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(name = "logon_name", length = 256, nullable = false)
    private String logonName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private SecActionStatus status;

    @Column(name = "description", length = 4000)
    private String description;

    public AuthAudit() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public Long getLogonId() {
        return logonId;
    }

    public void setLogonId(final Long logonId) {
        this.logonId = logonId;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public void setSessionUuid(final String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public SecEvent getActionType() {
        return actionType;
    }

    public void setActionType(final SecEvent actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(final LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public String getLogonName() {
        return logonName;
    }

    public void setLogonName(final String logonName) {
        this.logonName = logonName;
    }

    public SecActionStatus getStatus() {
        return status;
    }

    public void setStatus(final SecActionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}