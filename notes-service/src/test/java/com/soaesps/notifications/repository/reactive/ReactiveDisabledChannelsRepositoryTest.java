package com.soaesps.notifications.repository.reactive;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Integration data tests for ReactiveDisabledChannelsRepository using Spring Data R2DBC.
 * Validates non-blocking SQL queries and reactive stream behavior via StepVerifier.
 */
@DataR2dbcTest(excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class
})
@ContextConfiguration(classes = {
        ReactiveDisabledChannelsRepositoryTest.MiniApplication.class,
        R2dbcAutoConfiguration.class,
        R2dbcDataAutoConfiguration.class,
        R2dbcRepositoriesAutoConfiguration.class,
        R2dbcTransactionManagerAutoConfiguration.class
})
@DisplayName("Reactive Disabled Channels Repository Integration Test")
class ReactiveDisabledChannelsRepositoryTest {

    @TestConfiguration
    @EnableAutoConfiguration
    @EnableR2dbcRepositories(basePackageClasses = ReactiveDisabledChannelsRepository.class)
    @EntityScan(basePackages = {
            "com.soaesps.notifications.domain"
    })
    static class MiniApplication {
        @Bean
        public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
            ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
            initializer.setConnectionFactory(connectionFactory);

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));

            initializer.setDatabasePopulator(populator);
            return initializer;
        }
    }

    @Autowired
    private ReactiveDisabledChannelsRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Clean up the table before each test execution to ensure data isolation
        databaseClient.sql("DELETE FROM user_disabled_channels").then().block();
    }

    @Test
    @DisplayName("Should successfully retrieve all muted channels for a specific user ID")
    void shouldFindChannelsByUserId() {
        // Given: Seed test records into the single-table schema using non-blocking SQL execution
        Long testUserId = 777L;

        databaseClient.sql("INSERT INTO user_disabled_channels (user_id, channel) VALUES (:userId, :channel)")
                .bind("userId", testUserId)
                .bind("channel", "SMS")
                .then()
                .thenMany(databaseClient.sql("INSERT INTO user_disabled_channels (user_id, channel) VALUES (:userId, :channel)")
                        .bind("userId", testUserId)
                        .bind("channel", "PUSH")
                        .then())
                .then()
                .block(); // Block only inside the setup step to prepare test state

        // When: Invoke the custom reactive @Query method returning a Flux stream
        Flux<String> disabledChannelsFlux = repository.findChannelsByUserId(testUserId);

        // Then: Validate the reactive stream pipeline processing events sequentially without dropping exceptions
        StepVerifier.create(disabledChannelsFlux)
                .expectNext("SMS")
                .expectNext("PUSH")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty stream when no disabled channels exist for the given user ID")
    void shouldReturnEmptyStreamWhenUserHasNoDisabledChannels() {
        // Given: Target user has no records inside the database table
        Long nonExistentUserId = 999L;

        // When: Fetch muted configurations from R2DBC engine
        Flux<String> emptyFlux = repository.findChannelsByUserId(nonExistentUserId);

        // Then: Ensure the stream emits zero elements and immediately signals completion
        StepVerifier.create(emptyFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
}