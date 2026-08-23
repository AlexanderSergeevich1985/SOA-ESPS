package com.soaesps.msgprocess.config;

import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.BridgeFrom;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Integration pipeline for frame inference:
 *
 * <pre>
 *   Kafka [raw-frames]
 *      │  KafkaMessageDrivenChannelAdapter (concurrent listener)
 *      ▼
 *   kafkaInputChannel
 *      │
 *      ▼
 *   InferenceTransformer  (preprocess + Triton gRPC)
 *      │
 *      ├── success ──► kafkaOutboundChannel ──► Kafka [inference-results]
 *      └── failure ──► dlqOutboundChannel   ──► Kafka [frames-dlq]
 * </pre>
 *
 * <p>Concurrent listener containers (one per partition) provide horizontal scale-out
 * for the target throughput of 5000 frames/sec.
 */
@Configuration
@EnableIntegration
public class IntegrationConfiguration {

    private static final String INPUT_TOPIC_NAME = "raw-frames";
    private static final String OUTPUT_TOPIC_NAME = "inference-results";
    private static final String DLQ_TOPIC_NAME = "frames-dlq";

    public static final String KAFKA_INPUT_CHANNEL = "kafkaInputChannel";
    public static final String KAFKA_OUTBOUND_CHANNEL = "kafkaOutboundChannel";
    public static final String DLQ_OUTBOUND_CHANNEL = "dlqOutboundChannel";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String producerKeySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String producerValueSerializer;

    @Value("${spring.kafka.producer.acks}")
    private String producerAcks;

    @Value("${spring.kafka.producer.retries}")
    private int producerRetries;

    @Value("${app.pipeline.listener-concurrency:8}")
    private int listenerConcurrency;

    // =========================================================================
    // CHANNELS
    // =========================================================================

    @Bean
    public MessageChannel kafkaInputChannel() {
        return new DirectChannel();
    }

    /** PubSub so both success and DLQ paths can observe the same pipeline events
     *  (useful for metrics, logging, tracing). */
    @Bean
    public MessageChannel pubSubPipelineChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @BridgeFrom(value = "pubSubPipelineChannel")
    public MessageChannel kafkaOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @BridgeFrom(value = "pubSubPipelineChannel")
    public MessageChannel dlqOutboundChannel() {
        return new DirectChannel();
    }

    // =========================================================================
    // KAFKA INBOUND (concurrent, record-per-message)
    // =========================================================================

    /**
     * Overrides the shared container factory settings for THIS service only:
     * - concurrency = number of polling threads (ideally = number of topic partitions)
     * - AckMode.RECORD: offset committed after each successful handler invocation
     */
    @Bean
    public KafkaMessageDrivenChannelAdapter<String, MsgIOTDevice> kafkaInboundAdapter(@Qualifier("iotKafkaListenerContainerFactory")
            ConcurrentKafkaListenerContainerFactory<String, MsgIOTDevice> factory) {

        factory.setConcurrency(listenerConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        var container = factory.createContainer(INPUT_TOPIC_NAME);

        KafkaMessageDrivenChannelAdapter<String, MsgIOTDevice> adapter =
                new KafkaMessageDrivenChannelAdapter<>(container,
                        KafkaMessageDrivenChannelAdapter.ListenerMode.record);

        adapter.setOutputChannelName(KAFKA_INPUT_CHANNEL);
        return adapter;
    }

    // =========================================================================
    // PIPELINE: transformer is wired declaratively via @Transformer on InferenceTransformer
    // =========================================================================
    // (no explicit bean here — @Component + @Transformer inside InferenceTransformer
    //  handles the channel routing automatically)

    // =========================================================================
    // KAFKA OUTBOUND (results)
    // =========================================================================

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerKeySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerValueSerializer);
        props.put(ProducerConfig.ACKS_CONFIG, producerAcks);
        props.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);           // batch outbound for throughput
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1_048_576);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean
    @ServiceActivator(inputChannel = KAFKA_OUTBOUND_CHANNEL)
    public MessageHandler kafkaOutbound() {
        KafkaProducerMessageHandler<String, Object> handler =
                new KafkaProducerMessageHandler<>(kafkaTemplate());
        handler.setTopicExpression(
                new org.springframework.expression.common.LiteralExpression(OUTPUT_TOPIC_NAME));
        return handler;
    }

    // =========================================================================
    // DLQ OUTBOUND (malformed frames and inference failures)
    // =========================================================================

    @Bean
    @ServiceActivator(inputChannel = DLQ_OUTBOUND_CHANNEL)
    public MessageHandler dlqOutbound() {
        KafkaProducerMessageHandler<String, Object> handler =
                new KafkaProducerMessageHandler<>(kafkaTemplate());
        handler.setTopicExpression(
                new org.springframework.expression.common.LiteralExpression(DLQ_TOPIC_NAME));
        return handler;
    }
}