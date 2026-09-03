package com.soaesps.notifications.domain.reactive;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

/**
 * Immutable Record representing only the PUSH contact channel data for R2DBC.
 */
@Table("user_contacts")
public record PushContactRow(
        @Id Long id,
        @Column("user_id") Long userId,
        @Column("contact_type") String contactType,
        @Column("is_active") boolean active,
        @Column("is_primary") boolean primary,
        @Column("created_at") LocalDateTime createdAt,
        @Column("push_token") String pushToken,
        @Column("device_id") String deviceId,
        @Column("device_type") String deviceType
) {
    public PushContactRow {
        if (contactType == null) contactType = "PUSH";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}