package com.soaesps.notifications.repository.reactive;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * Highly isolated integration test base for reactive repositories supporting dynamic schema script injection.
 * Explicitly removes Spring Boot's Mockito listeners to prevent Windows Cyrillic path bugs.
 */
@DataR2dbcTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.config.import=optional:configserver:",
        "spring.autoconfigure.exclude=org.springframework.cloud.config.client.ConfigClientAutoConfiguration,org.springframework.cloud.config.client.ConfigServerBootstrapConfiguration",
        "eureka.client.enabled=false",
        "spring.r2dbc.url=r2dbc:h2:mem:///test_reactive_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.config.import=optional:configserver:"
})
@ActiveProfiles("TEST")
@ContextConfiguration(classes = BaseReactiveRepositoryTest.MiniApplication.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
})
public abstract class BaseReactiveRepositoryTest {

    /**
     * Protected constructor allowing child classes to explicitly define their targeting schema SQL file.
     * Sets a system property to bridge the value into the static TestConfiguration class.
     *
     * @param schemaScriptName Name of the schema script file (e.g., "notification_schema.sql")
     */
    protected BaseReactiveRepositoryTest(String schemaScriptName) {
        System.setProperty("test.reactive.schema.script", schemaScriptName);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableR2dbcRepositories(basePackages = "com.soaesps.notifications.repository")
    @EntityScan(basePackages = {
            "com.soaesps.notifications.dto",
            "com.soaesps.notifications.repository",
            "com.soaesps.notifications"
    })
    static class MiniApplication {

        /**
         * Orchestrates schema execution using a dynamically provided script name evaluated from Environment.
         */
        @Bean
        public ConnectionFactoryInitializer initializer(
                ConnectionFactory connectionFactory,
                @Value("${test.reactive.schema.script:schema.sql}") String schemaScript) {

            ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
            initializer.setConnectionFactory(connectionFactory);

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource(schemaScript));

            initializer.setDatabasePopulator(populator);
            return initializer;
        }
    }
}