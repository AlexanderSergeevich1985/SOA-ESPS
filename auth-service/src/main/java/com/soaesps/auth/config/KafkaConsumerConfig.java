package com.soaesps.auth.config;

import com.soaesps.core.dto.AuthRegistrationPayload;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Infrastructure configuration for the Kafka Consumer layer in auth-service.
 * Configures the container factory for processing critical security registration flows.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Primary container factory for handling critical authentication registration events.
     * Enforces single-record processing with MANUAL_IMMEDIATE acknowledgment mode
     * to guarantee that messages are only acknowledged after a successful database commit.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuthRegistrationPayload> authListenerFactory(
            ConsumerFactory<String, AuthRegistrationPayload> authConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, AuthRegistrationPayload> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(authConsumerFactory);

        // Disable batch listening to process messages one by one
        factory.setBatchListener(false);

        // Enable manual offsets tracking to prevent data loss on DB failures
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    /**
     * Base consumer settings customized for safe deserialization of AuthRegistrationPayload objects.
     */
    @Bean
    public ConsumerFactory<String, AuthRegistrationPayload> authConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Security package boundary setup preventing class deserialization crashes
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.soaesps.core.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AuthRegistrationPayload.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(AuthRegistrationPayload.class)
        );
    }
}