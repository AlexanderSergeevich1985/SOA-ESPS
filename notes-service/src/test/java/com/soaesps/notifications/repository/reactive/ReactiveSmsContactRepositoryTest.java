package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.SmsContactRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration data tests for ReactiveSmsContactRepository using Spring Data R2DBC.
 * Validates non-blocking CRUD operations and isolated SQL filtering using SmsContactRow Record.
 */
@DisplayName("Reactive SMS Contact Repository Integration Test")
class ReactiveSmsContactRepositoryTest extends BaseReactiveRepositoryTest {

    /**
     * Pass the custom schema file specific to this repository or module boundary.
     */
    public ReactiveSmsContactRepositoryTest() {
        super("schema_user_contact.sql");
    }

    @Autowired
    private ReactiveSmsContactRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Clean up the polymorphic table before each test execution to ensure data isolation
        databaseClient.sql("DELETE FROM user_contacts").then().block();
    }

    @Test
    @DisplayName("Should successfully save an immutable SMS contact record and generate id")
    void shouldSaveSmsContact() {
        // Given: An immutable Record instance with null ID to signal an INSERT operation
        SmsContactRow newSms = new SmsContactRow(
                null,
                333L,
                "SMS",
                true,
                true,
                LocalDateTime.now(),
                "+12345678901"
        );

        // When: Save the Record through reactive CRUD repository
        Mono<SmsContactRow> savedMono = repository.save(newSms);

        // Then: Verify that a new Record copy is returned with a generated ID from the database auto-increment
        StepVerifier.create(savedMono)
                .assertNext(savedRecord -> {
                    assertNotNull(savedRecord.id());
                    assertEquals(333L, savedRecord.userId());
                    assertEquals("SMS", savedRecord.contactType());
                    assertEquals("+12345678901", savedRecord.phoneNumber());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully retrieve only SMS contacts and filter out other channel types")
    void shouldFindOnlySmsContactsByUserId() {
        // Given: Seed mixed contact types (SMS and TELEGRAM) to verify proper isolation by discriminator
        Long testUserId = 555L;

        databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, phone_number, is_active, is_primary) " +
                        "VALUES (:userId, 'SMS', '+79991112233', true, true)")
                .bind("userId", testUserId)
                .then()
                .thenMany(databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, phone_number, is_active, is_primary) " +
                                "VALUES (:userId, 'SMS', '+79994445566', true, false)")
                        .bind("userId", testUserId)
                        .then())
                .thenMany(databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, telegram_chat_id, is_active, is_primary) " +
                                "VALUES (:userId, 'TELEGRAM', '987654321', true, false)")
                        .bind("userId", testUserId)
                        .then())
                .then()
                .block();

        // When: Invoke custom native query method designed for the SMS domain flow
        Flux<SmsContactRow> smsFlux = repository.findSmsByUserId(testUserId);

        // Then: Assert that exactly two SMS records are returned sequentially, ignoring the TELEGRAM row completely
        StepVerifier.create(smsFlux)
                .assertNext(row -> {
                    assertEquals("SMS", row.contactType());
                    assertEquals("+79991112233", row.phoneNumber());
                })
                .assertNext(row -> {
                    assertEquals("SMS", row.contactType());
                    assertEquals("+79994445566", row.phoneNumber());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty stream when no SMS records match the target user id")
    void shouldReturnEmptyFluxWhenUserHasNoSmsContacts() {
        // Given: User exists with a TELEGRAM contact, but has no SMS record configured
        Long targetUserId = 444L;
        databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, telegram_chat_id, is_active, is_primary) " +
                        "VALUES (:userId, 'TELEGRAM', '123456789', true, true)")
                .bind("userId", targetUserId)
                .then()
                .block();

        // When: Look up SMS configurations
        Flux<SmsContactRow> resultFlux = repository.findSmsByUserId(targetUserId);

        // Then: Ensure zero elements are delivered down the pipeline and it cleanly completes
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
}