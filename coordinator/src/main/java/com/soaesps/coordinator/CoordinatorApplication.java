package com.soaesps.coordinator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Universal Group Message Coordinator application.
 *
 * This Spring Boot application is responsible for:
 * 1. Causal ordering of distributed group messages using Vector Clocks.
 * 2. Conflict resolution for concurrent updates.
 * 3. Geo-replication of message states across distributed data centers via Kafka.
 *
 * @author SOAESPS Team
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class CoordinatorApplication {

    /**
     * The main method that serves as the entry point for the Spring Boot application.
     * It bootstraps the application context, initializes the embedded web server,
     * and starts the background scheduling tasks (e.g., synchronization cycles).
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(CoordinatorApplication.class, args);
    }
}