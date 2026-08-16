package com.soaesps.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
@EntityScan({"com.soaesps.core.DataModels"})
@EnableJpaRepositories(basePackages = {"com.soaesps.profile.repository"})
@EnableTransactionManagement
public class ProfileApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ProfileApplication.class, args);
    }
}