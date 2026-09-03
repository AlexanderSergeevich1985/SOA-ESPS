package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.PushContactRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

/**
 * Integration data tests for ReactivePushContactRepository using Spring Data R2DBC.
 * Validates non-blocking CRUD operations and isolated SQL filtering using PushContactRow Record.
 */
@DisplayName("Reactive Push Contact Repository Integration Test")
class ReactivePushContactRepositoryTest extends BaseReactiveRepositoryTest {

    /**
     * Pass the custom schema file specific to this repository or module boundary.
     */
    public ReactivePushContactRepositoryTest() {
        super("schema_user_contact.sql");
    }

    @Autowired
    private ReactivePushContactRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Clean up the polymorphic table before each test execution to ensure data isolation
        databaseClient.sql("DELETE FROM user_contacts").then().block();
    }

    @Test
    @DisplayName("Should successfully save an immutable push contact record and generate id")
    void shouldSavePushContact() {
        // Given: An immutable Record instance with null ID to signal an INSERT operation
        PushContactRow newPush = new PushContactRow(
                null,
                222L,
                "PUSH",
                true,
                false,
                LocalDateTime.now(),
                "fcm-token-abc-123",
                "device-id-xyz",
                "ANDROID"
        );

        // When: Save the Record through reactive CRUD repository
        Mono<PushContactRow> savedMono = repository.save(newPush);

        // Then: Verify that a new Record copy is returned with a generated ID from the database auto-increment
        StepVerifier.create(savedMono)
                .assertNext(savedRecord -> {
                    org.junit.jupiter.api.Assertions.assertNotNull(savedRecord.id());
                    org.junit.jupiter.api.Assertions.assertEquals(222L, savedRecord.userId());
                    org.junit.jupiter.api.Assertions.assertEquals("PUSH", savedRecord.contactType());
                    org.junit.jupiter.api.Assertions.assertEquals("fcm-token-abc-123", savedRecord.pushToken());
                    org.junit.jupiter.api.Assertions.assertEquals("device-id-xyz", savedRecord.deviceId());
                    org.junit.jupiter.api.Assertions.assertEquals("ANDROID", savedRecord.deviceType());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully retrieve only PUSH contacts and filter out other channel types")
    void shouldFindOnlyPushContactsByUserId() {
        // Given: Seed mixed contact types (PUSH and EMAIL) to verify proper isolation by discriminator
        Long testUserId = 888L;

        databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, push_token, device_id, device_type, is_active, is_primary) " +
                        "VALUES (:userId, 'PUSH', 'fcm-token-primary', 'dev-01', 'IOS', true, true)")
                .bind("userId", testUserId)
                .then()
                .thenMany(databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, push_token, device_id, device_type, is_active, is_primary) " +
                                "VALUES (:userId, 'PUSH', 'fcm-token-secondary', 'dev-02', 'WEB', true, false)")
                        .bind("userId", testUserId)
                        .then())
                .thenMany(databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, email_address, is_active, is_primary) " +
                                "VALUES (:userId, 'EMAIL', 'user@example.com', true, false)")
                        .bind("userId", testUserId)
                        .then())
                .then()
                .block();

        // When: Invoke custom native query method designed for the push domain flow
        Flux<PushContactRow> pushFlux = repository.findPushByUserId(testUserId);

        // Then: Assert that exactly two PUSH records are returned sequentially, ignoring the EMAIL row completely
        StepVerifier.create(pushFlux)
                .assertNext(row -> {
                    org.junit.jupiter.api.Assertions.assertEquals("PUSH", row.contactType());
                    org.junit.jupiter.api.Assertions.assertEquals("fcm-token-primary", row.pushToken());
                    org.junit.jupiter.api.Assertions.assertEquals("dev-01", row.deviceId());
                    org.junit.jupiter.api.Assertions.assertEquals("IOS", row.deviceType());
                })
                .assertNext(row -> {
                    org.junit.jupiter.api.Assertions.assertEquals("PUSH", row.contactType());
                    org.junit.jupiter.api.Assertions.assertEquals("fcm-token-secondary", row.pushToken());
                    org.junit.jupiter.api.Assertions.assertEquals("dev-02", row.deviceId());
                    org.junit.jupiter.api.Assertions.assertEquals("WEB", row.deviceType());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty stream when no push records match the target user id")
    void shouldReturnEmptyFluxWhenUserHasNoPushContacts() {
        // Given: User exists with an EMAIL contact, but has no PUSH record configured
        Long targetUserId = 999L;
        databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, email_address, is_active, is_primary) " +
                        "VALUES (:userId, 'EMAIL', 'test@example.com', true, true)")
                .bind("userId", targetUserId)
                .then()
                .block();

        // When: Look up push device configurations
        Flux<PushContactRow> resultFlux = repository.findPushByUserId(targetUserId);

        // Then: Ensure zero elements are delivered down the pipeline and it cleanly completes
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
}