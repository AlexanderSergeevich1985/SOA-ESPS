package com.soaesps.schedulerservice;

import com.soaesps.core.security.config.BaseSecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the scheduler microservice.
 *
 * <p>Security is provided by the standard dual-auth configuration
 * ({@link BaseSecurityConfiguration}): mTLS for inter-service calls
 * and optional JWT for end-user access, configurable via application.yml.
 *
 * <p>{@link EnableScheduling} activates the {@code @Scheduled} / {@code TaskScheduler}
 * infrastructure used by {@code SchedulerServiceImpl}.
 */
//@PropertySource("classpath:config/application.yml")
@SpringBootApplication
@EnableScheduling
@Import(BaseSecurityConfiguration.class)
public class SchedulerApplication {

    private static final Logger log = LoggerFactory.getLogger(SchedulerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
        log.info("Scheduler service started");
    }
}