package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.EmailContactRow;
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
 * Integration data tests for ReactiveEmailContactRepository using Spring Data R2DBC.
 * Validates non-blocking CRUD operations and isolated SQL filtering using EmailContactRow Record.
 */
@DisplayName("Reactive Email Contact Repository Integration Test")
class ReactiveEmailContactRepositoryTest extends BaseReactiveRepositoryTest {

    /**
     * Pass the custom schema file specific to this repository or module boundary.
     */
    public ReactiveEmailContactRepositoryTest() {
        super("schema_user_contact.sql");
    }

    @Autowired
    private ReactiveEmailContactRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Clean up the polymorphic table before each test execution to ensure data isolation
        databaseClient.sql("DELETE FROM user_contacts").then().block();
    }

    @Test
    @DisplayName("Should successfully save an immutable email contact record and generate id")
    void shouldSaveEmailContact() {
        // Given: An immutable Record instance with null ID to signal an INSERT operation
        EmailContactRow newEmail = new EmailContactRow(
                null,
                111L,
                "EMAIL",
                true,
                true,
                LocalDateTime.now(),
                "save-test@example.com"
        );

        // When: Save the Record through reactive CRUD repository
        Mono<EmailContactRow> savedMono = repository.save(newEmail);

        // Then: Verify that a new Record copy is returned with a generated ID from the database auto-increment
        StepVerifier.create(savedMono)
                .assertNext(savedRecord -> {
                    org.junit.jupiter.api.Assertions.assertNotNull(savedRecord.id());
                    org.junit.jupiter.api.Assertions.assertEquals(111L, savedRecord.userId());
                    org.junit.jupiter.api.Assertions.assertEquals("EMAIL", savedRecord.contactType());
                    org.junit.jupiter.api.Assertions.assertEquals("save-test@example.com", savedRecord.emailAddress());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully retrieve only EMAIL contacts and filter out other channel types")
    void shouldFindOnlyEmailContactsByUserId() {
        // Given: Seed mixed contact types (EMAIL and SMS) to verify proper isolation by discriminator
        Long testUserId = 777L;

        databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, email_address, is_active, is_primary) " +
                        "VALUES (:userId, 'EMAIL', 'user-primary@example.com', true, true)")
                .bind("userId", testUserId)
                .then()
                .thenMany(databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, email_address, is_active, is_primary) " +
                                "VALUES (:userId, 'EMAIL', 'user-work@example.com', true, false)")
                        .bind("userId", testUserId)
                        .then())
                .thenMany(databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, phone_number, is_active, is_primary) " +
                                "VALUES (:userId, 'SMS', '+1234567890', true, false)")
                        .bind("userId", testUserId)
                        .then())
                .then()
                .block();

        // When: Invoke custom native query method designed for the email domain flow
        Flux<EmailContactRow> emailFlux = repository.findEmailByUserId(testUserId);

        // Then: Assert that exactly two EMAIL records are returned sequentially, ignoring the SMS row completely
        StepVerifier.create(emailFlux)
                .assertNext(row -> {
                    org.junit.jupiter.api.Assertions.assertEquals("EMAIL", row.contactType());
                    org.junit.jupiter.api.Assertions.assertEquals("user-primary@example.com", row.emailAddress());
                })
                .assertNext(row -> {
                    org.junit.jupiter.api.Assertions.assertEquals("EMAIL", row.contactType());
                    org.junit.jupiter.api.Assertions.assertEquals("user-work@example.com", row.emailAddress());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty stream when no email records match the target user id")
    void shouldReturnEmptyFluxWhenUserHasNoEmailContacts() {
        // Given: User exists with an SMS contact, but has no EMAIL record configured
        Long targetUserId = 888L;
        databaseClient.sql("INSERT INTO user_contacts (user_id, contact_type, phone_number, is_active, is_primary) " +
                        "VALUES (:userId, 'SMS', '+1234567890', true, true)")
                .bind("userId", targetUserId)
                .then()
                .block();

        // When: Look up email addresses
        Flux<EmailContactRow> resultFlux = repository.findEmailByUserId(targetUserId);

        // Then: Ensure zero elements are delivered down the pipeline and it cleanly completes
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
}