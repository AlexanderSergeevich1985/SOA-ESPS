package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.NotificationTemplateRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration data tests for ReactiveNotificationTemplateRepository using Spring Data R2DBC.
 * Validates precise native SQL execution matching composite key bounds (notification_type, channel_type).
 */
@DisplayName("Reactive Notification Template Repository Integration Test")
class ReactiveNotificationTemplateRepositoryTest extends BaseReactiveRepositoryTest {

    /**
     * Pass the custom schema file specific to this repository or module boundary.
     */
    public ReactiveNotificationTemplateRepositoryTest() {
        super("notification_templates.sql");
    }

    @Autowired
    private ReactiveNotificationTemplateRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Clean up the dictionary table before each test execution to guarantee data isolation
        databaseClient.sql("DELETE FROM notification_templates").then().block();
    }

    @Test
    @DisplayName("Should successfully retrieve template metadata using composite key query path")
    void shouldFindTemplateMetaByCompositeKey() {
        // Given: Seed database with an inline text template configuration row
        String targetType = "PAYMENT_SUCCESS";
        String targetChannel = "SMS";
        String sampleTemplate = "Hello {username}, paid {amount} USD.";

        databaseClient.sql("INSERT INTO notification_templates (notification_type, channel_type, is_external_storage, inline_text_template) " +
                        "VALUES (:notificationType, :channelType, false, :templateText)")
                .bind("notificationType", targetType)
                .bind("channelType", targetChannel)
                .bind("templateText", sampleTemplate)
                .then()
                .block();

        // When: Execute the custom reactive repository method passing composite bounds
        Mono<NotificationTemplateRow> metaMono = repository.findTemplateMeta(targetType, targetChannel);

        // Then: Validate precise field mappings inside the StepVerifier stream lifecycle assertion
        StepVerifier.create(metaMono)
                .assertNext(row -> {
                    org.junit.jupiter.api.Assertions.assertEquals(targetType, row.notificationType());
                    org.junit.jupiter.api.Assertions.assertEquals(targetChannel, row.channelType());
                    org.junit.jupiter.api.Assertions.assertFalse(row.externalStorage());
                    org.junit.jupiter.api.Assertions.assertEquals(sampleTemplate, row.inlineTextTemplate());
                    org.junit.jupiter.api.Assertions.assertNull(row.minioObjectKey());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully retrieve external MinIO storage path details for heavy templates")
    void shouldFindExternalMinioTemplateMeta() {
        // Given: Seed an external storage configuration layout specific to heavy email HTML formats
        String targetType = "SECURITY_ALERT";
        String targetChannel = "EMAIL";
        String expectedMinioKey = "templates/email/security_alert_v1.html";

        databaseClient.sql("INSERT INTO notification_templates (notification_type, channel_type, is_external_storage, minio_object_key) " +
                        "VALUES (:notificationType, :channelType, true, :minioKey)")
                .bind("notificationType", targetType)
                .bind("channelType", targetChannel)
                .bind("minioKey", expectedMinioKey)
                .then()
                .block();

        // When: Invoke metadata finder operator
        Mono<NotificationTemplateRow> metaMono = repository.findTemplateMeta(targetType, targetChannel);

        // Then: Ensure flags and structural pointers evaluate correctly
        StepVerifier.create(metaMono)
                .assertNext(row -> {
                    org.junit.jupiter.api.Assertions.assertEquals(targetType, row.notificationType());
                    org.junit.jupiter.api.Assertions.assertEquals(targetChannel, row.channelType());
                    org.junit.jupiter.api.Assertions.assertTrue(row.externalStorage());
                    org.junit.jupiter.api.Assertions.assertEquals(expectedMinioKey, row.minioObjectKey());
                    org.junit.jupiter.api.Assertions.assertNull(row.inlineTextTemplate());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty mono signal when composite identifier has no matching records")
    void shouldReturnEmptyMonoWhenTemplateNotFound() {
        // When: Fetch template metadata for keys that do not exist inside СУБД dictionary
        Mono<NotificationTemplateRow> emptyMono = repository.findTemplateMeta("NON_EXISTENT_EVENT", "SMS");

        // Then: Non-blocking stream pipeline completes with zero element delivery signals
        StepVerifier.create(emptyMono)
                .expectNextCount(0)
                .verifyComplete();
    }
}