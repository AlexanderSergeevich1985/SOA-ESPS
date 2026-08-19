package com.soaesps.notifications;

import com.soaesps.core.config.KafkaConsumerConfig;
import com.soaesps.core.integration.listener.UniversalKafkaListener;
import com.soaesps.core.security.config.BaseSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@Import({BaseSecurityConfiguration.class, KafkaConsumerConfig.class, UniversalKafkaListener.class})
public class NotesApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotesApplication.class, args);
    }
}