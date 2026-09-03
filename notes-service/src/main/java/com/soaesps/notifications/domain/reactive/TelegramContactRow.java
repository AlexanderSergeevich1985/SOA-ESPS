package com.soaesps.notifications.domain.reactive;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

/**
 * Immutable Record representing only the TELEGRAM contact channel data for R2DBC.
 */
@Table("user_contacts")
public record TelegramContactRow(
        @Id Long id,
        @Column("user_id") Long userId,
        @Column("contact_type") String contactType,
        @Column("is_active") boolean active,
        @Column("is_primary") boolean primary,
        @Column("created_at") LocalDateTime createdAt,
        @Column("telegram_chat_id") String telegramChatId,
        @Column("telegram_username") String telegramUsername
) {
    public TelegramContactRow {
        if (contactType == null) contactType = "TELEGRAM";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}