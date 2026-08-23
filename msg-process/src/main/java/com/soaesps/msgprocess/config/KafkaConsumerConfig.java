package com.soaesps.msgprocess.config;

import com.soaesps.core.config.BaseKafkaConsumerConfig;
import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
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
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServerAddress;

    @Value("${spring.kafka.consumer.iot-group-id}")
    private String iotConsumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String consumerAutoOffsetReset;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String consumerKeyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String consumerValueDeserializer;

    @Bean
    public ConsumerFactory<String, MsgIOTDevice> iotConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Map all standard consumer configurations from your properties
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, iotConsumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);

        // Pass class names as strings to let Kafka architecture manage deserializer initialization fallback
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerKeyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Configure target JSON deserializer for the universal JsonNode class
        JsonDeserializer<MsgIOTDevice> jsonDeserializer = new JsonDeserializer<>(MsgIOTDevice.class);
        jsonDeserializer.addTrustedPackages("*"); // Safe for JsonNode since it represents raw JSON tree
        jsonDeserializer.setUseTypeHeaders(false); // Ignore sender type headers to avoid package mismatch errors

        // Configure ErrorHandlingDeserializer to prevent infinite loops caused by malformed JSON data (Poison Pills)
        ErrorHandlingDeserializer<MsgIOTDevice> errorHandlingValueDeserializer =
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
    @Bean("iotKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, MsgIOTDevice> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MsgIOTDevice> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(iotConsumerFactory());
        return factory;
    }
}