package com.soaesps.aggregator.config;

import com.soaesps.aggregator.domain.MlMetricEvent;
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
 * Infrastructure configuration for the Kafka Consumer layer.
 * Registers the missing 'mlBatchListenerFactory' required by high-throughput metrics ingestion writers.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Primary factory bean matching containerFactory = "mlBatchListenerFactory" string selector.
     * Expressly activates internal batch polling mode to execute single DB round-trips.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MlMetricEvent> mlBatchListenerFactory(
            ConsumerFactory<String, MlMetricEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, MlMetricEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // CRITICAL FIX: Explicitly enforce high-throughput batching injection array to the listener
        factory.setBatchListener(true);

        // Wire explicit MANUAL_IMMEDIATE acknowledgment settings matching your code's ack.acknowledge() loops
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    /**
     * Internal consumer baseline layout settings. Normalizes deserializers to read MlMetricEvent payloads cleanly.
     */
    @Bean
    public ConsumerFactory<String, MlMetricEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Safety properties preventing runtime payload cast exceptions
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.soaesps.aggregator.domain");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MlMetricEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(MlMetricEvent.class));
    }
}