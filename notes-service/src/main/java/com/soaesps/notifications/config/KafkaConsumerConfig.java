package com.soaesps.notifications.config;

import com.soaesps.core.config.BaseKafkaConsumerConfig;
import com.soaesps.notifications.dto.InboundNotificationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Import({BaseKafkaConsumerConfig.class})
public class KafkaConsumerConfig {
    public static final String INBOUND_NOTIFICATION_KAFKA_CONTAINER = "inNotesKafkaListenerContainerFactory";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServerAddress;

    @Value("${spring.kafka.consumer.inbound-notes-group-id:inbound-notes-group-id}")
    private String inNotesConsumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String consumerAutoOffsetReset;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String consumerKeyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String consumerValueDeserializer;

    @Bean
    public ConsumerFactory<String, InboundNotificationEvent> inNotesConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Map all standard consumer configurations from your properties
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, inNotesConsumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);

        // Pass class names as strings to let Kafka architecture manage deserializer initialization fallback
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerKeyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Configure target JSON deserializer for the universal JsonNode class
        JsonDeserializer<InboundNotificationEvent> jsonDeserializer = new JsonDeserializer<>(InboundNotificationEvent.class);
        jsonDeserializer.addTrustedPackages("*"); // Safe for JsonNode since it represents raw JSON tree
        jsonDeserializer.setUseTypeHeaders(false); // Ignore sender type headers to avoid package mismatch errors

        // Configure ErrorHandlingDeserializer to prevent infinite loops caused by malformed JSON data (Poison Pills)
        ErrorHandlingDeserializer<InboundNotificationEvent> errorHandlingValueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        ErrorHandlingDeserializer<String> errorHandlingKeyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());

        // Return factory utilizing delegation patterns for safe deserialization
        return new DefaultKafkaConsumerFactory<>(
                props,
                errorHandlingKeyDeserializer,
                errorHandlingValueDeserializer
        );
    }

    @Primary
    @Bean(INBOUND_NOTIFICATION_KAFKA_CONTAINER)
    public ConcurrentKafkaListenerContainerFactory<String, InboundNotificationEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InboundNotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inNotesConsumerFactory());
        return factory;
    }
}