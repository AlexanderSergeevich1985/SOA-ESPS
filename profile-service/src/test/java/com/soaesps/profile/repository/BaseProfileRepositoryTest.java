package com.soaesps.profile.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * Highly isolated integration test base for PROFILE module repositories.
 * Explicitly removes Spring Boot's Mockito listeners to prevent Windows Cyrillic path bugs.
 */
@DataJpaTest
@ActiveProfiles("TEST")
@ContextConfiguration(classes = BaseProfileRepositoryTest.MiniApplication.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
public abstract class BaseProfileRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = "com.soaesps.profile.repository")
    @EntityScan(basePackages = {
            "com.soaesps.profile",
            "com.soaesps.core.DataModels.device",
            "com.soaesps.core.DataModels.user"
    })
    static class MiniApplication {
    }
}