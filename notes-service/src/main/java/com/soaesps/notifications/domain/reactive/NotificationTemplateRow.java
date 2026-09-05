package com.soaesps.notifications.domain.reactive;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Immutable Record representing the 'notification_templates' table backed by a composite key.
 */
@Table("notification_templates")
public record NotificationTemplateRow(
        @Column("notification_type")
        String notificationType,

        @Column("channel_type")
        String channelType,

        @Column("is_external_storage")
        boolean externalStorage,

        @Column("inline_title_template")
        String inlineTitleTemplate,

        @Column("inline_text_template")
        String inlineTextTemplate,

        @Column("minio_object_key")
        String minioObjectKey
) {}
