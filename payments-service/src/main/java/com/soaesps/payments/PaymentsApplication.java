package com.soaesps.payments;

import com.soaesps.core.config.BaseKafkaConsumerConfig;
import com.soaesps.core.integration.listener.UniversalKafkaListener;
import com.soaesps.core.security.config.BaseSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.soaesps.payments")
@Import({BaseSecurityConfiguration.class, BaseKafkaConsumerConfig.class, UniversalKafkaListener.class})
public class PaymentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentsApplication.class, args);
    }
}