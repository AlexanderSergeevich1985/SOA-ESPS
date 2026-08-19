package com.soaesps.notifications.notifications;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Stores a device FCM token bound to a user account.
 */
@Entity
@Table(
        name = "device_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_user_id_token", columnNames = {"user_id", "token"})
        },
        indexes = {
                @Index(name = "idx_device_tokens_user_id", columnList = "user_id")
        }
)
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Changed from String (MongoDB) to Long/Bigint auto-increment for RDBMS

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FCM tokens can be long (up to 4096 bytes theoretically, usually ~160+ chars).
    // Specifying length = 512 or 1024 is safer than default varchar(255).
    @Column(name = "token", nullable = false, length = 1024)
    private String token;

    @Column(name = "platform", length = 32)
    private String platform; // ANDROID / IOS / WEB

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    // Mandatory no-arg constructor for JPA
    public DeviceToken() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}