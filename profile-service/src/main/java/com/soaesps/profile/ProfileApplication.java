package com.soaesps.profile;

import com.soaesps.core.config.KafkaConsumerConfig;
import com.soaesps.core.integration.listener.UniversalKafkaListener;
import com.soaesps.core.security.config.BaseSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableFeignClients(basePackages = "com.soaesps.profile")
@Import({BaseSecurityConfiguration.class, KafkaConsumerConfig.class, UniversalKafkaListener.class})
public class ProfileApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ProfileApplication.class, args);
    }
}