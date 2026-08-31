package com.soaesps.notifications.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * Highly isolated integration test base for PAYMENTS module repositories.
 * Explicitly removes Spring Boot's Mockito listeners to prevent Windows Cyrillic path bugs.
 */
@DataJpaTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.config.import=optional:configserver:",
        "spring.autoconfigure.exclude=org.springframework.cloud.config.client.ConfigClientAutoConfiguration,org.springframework.cloud.config.client.ConfigServerBootstrapConfiguration",
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "eureka.client.enabled=false",
        // 1. Force Hibernate to drop and recreate the schema for clean test execution
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // 2. Database driver and platform settings
        "spring.datasource.url=jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        // 3. Naming strategies configuration
        "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
        "spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl"
})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.config.import=optional:configserver:"
})
@ActiveProfiles("TEST")
@ContextConfiguration(classes = BaseNotificationRepositoryTest.MiniApplication.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
public abstract class BaseNotificationRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = "com.soaesps.notifications.repository")
    @EntityScan(basePackages = {
            "com.soaesps.notifications.domain",
            "com.soaesps.notifications"
    })
    static class MiniApplication {
    }
}