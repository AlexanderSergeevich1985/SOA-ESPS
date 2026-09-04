package com.soaesps.notifications.repository.reactive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Integration data tests for ReactiveNotificationTypeRepository using Spring Data R2DBC.
 * Validates dynamic filtering of supported message types based on their active status in DB.
 */
@DisplayName("Reactive Notification Type Repository Integration Test")
class ReactiveNotificationTypeRepositoryTest extends BaseReactiveRepositoryTest {

    /**
     * Pass the custom schema file specific to this repository or module boundary.
     */
    public ReactiveNotificationTypeRepositoryTest() {
        super("supported_notification_types.sql");
    }

    @Autowired
    private ReactiveNotificationTypeRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Clean up the target dictionary table before each test execution to ensure isolation
        databaseClient.sql("DELETE FROM supported_notification_types").then().block();
    }

    @Test
    @DisplayName("Should successfully retrieve only active notification type names from database")
    void shouldFindAllActiveNotificationTypes() {
        // Given: Seed database with active and inactive type records
        databaseClient.sql("INSERT INTO supported_notification_types (type_name, is_active) VALUES ('PAYMENT_SUCCESS', true)")
                .then()
                .thenMany(databaseClient.sql("INSERT INTO supported_notification_types (type_name, is_active) VALUES ('SECURITY_ALERT', true)").then())
                .thenMany(databaseClient.sql("INSERT INTO supported_notification_types (type_name, is_active) VALUES ('MARKETING_SPAM', false)").then())
                .then()
                .block();

        // When: Execute the reactive query fetching native String names
        Flux<String> activeTypesFlux = repository.findAllActiveTypes();

        // Then: Assert that only active event types are sequentially emitted down the stream pipeline
        StepVerifier.create(activeTypesFlux)
                .expectNext("PAYMENT_SUCCESS")
                .expectNext("SECURITY_ALERT")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty flux stream when no active notification types are configured")
    void shouldReturnEmptyFluxWhenNoActiveTypesExist() {
        // Given: Configuration contains only an inactive type entry
        databaseClient.sql("INSERT INTO supported_notification_types (type_name, is_active) VALUES ('OLD_DEPRECATED_ALERT', false)")
                .then()
                .block();

        // When: Query active configuration dictionary types
        Flux<String> emptyFlux = repository.findAllActiveTypes();

        // Then: Ensure zero elements are delivered and the non-blocking pipeline completes immediately
        StepVerifier.create(emptyFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
}
