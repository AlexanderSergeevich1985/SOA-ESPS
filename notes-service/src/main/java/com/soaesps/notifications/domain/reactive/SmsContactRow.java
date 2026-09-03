package com.soaesps.notifications.domain.reactive;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

/**
 * Immutable Record representing only the SMS contact channel data.
 */
@Table("user_contacts")
public record SmsContactRow(
        @Id Long id,
        @Column("user_id") Long userId,
        @Column("contact_type") String contactType, // Always "SMS"
        @Column("is_active") boolean active,
        @Column("is_primary") boolean primary,
        @Column("created_at") LocalDateTime createdAt,
        @Column("phone_number") String phoneNumber
) {
    public SmsContactRow {
        if (contactType == null) contactType = "SMS";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}