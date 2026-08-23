package com.soaesps.notifications.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.soaesps.core.config.KafkaConsumerConfig;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.component.aggregator.CorrelationStrategyI;
import com.soaesps.core.component.aggregator.ReleaseStrategyI;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("com.soaesps.notifications.integration")
@EnableIntegration
@IntegrationComponentScan("com.soaesps.notifications.integration")
@Import(KafkaConsumerConfig.class) // Imports your custom Kafka consumer beans
public class IntegrationConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Producer properties from YAML
    @Value("${spring.kafka.producer.key-serializer}")
    private String producerKeySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String producerValueSerializer;

    @Value("${spring.kafka.producer.acks}")
    private String producerAcks;

    @Value("${spring.kafka.producer.retries}")
    private int producerRetries;

    private static final String INPUT_TOPIC_NAME = "message_queue_inbound";
    private static final String OUTPUT_TOPIC_NAME = "message_queue_outbound";

    @Bean(IntegrationConstant.GATEWAY_CHANNEL)
    public MessageChannel gatewayChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.FILTER_CHANNEL)
    public MessageChannel filterChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.DISCARD_FILTER_CHANNEL)
    public MessageChannel discardFilterChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.TRANSFORMER_CHANNEL)
    public MessageChannel transformerChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.SIMPLE_ROUTER_CHANNEL)
    public MessageChannel simpleRouterChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.AGG_ROUTER_CHANNEL)
    public MessageChannel aggRouterChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.MESSAGE_ACTIVATOR_CHANNEL)
    public MessageChannel activatorChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel kafkaInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel pubSubFileChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @BridgeFrom(value = "pubSubFileChannel")
    public MessageChannel kafkaOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public ReleaseStrategyI releaseStrategy() {
        return new ReleaseStrategyI.LimitReleaseStrategy();
    }

    @Bean
    public CorrelationStrategyI correlationStrategy() {
        return new CorrelationStrategyI.CorrelationKeyStrategy((object) -> {
            try {
                final Message<?> message = (Message<?>) object;
                return CryptoHelper.getObjectDigest(message.getHeaders());
            } catch (final IOException | NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Failed to generate correlation key digest", ex);
            }
        });
    }

    // =========================================================================
    // KAFKA INBOUND CONFIGURATION (RECEIVING)
    // =========================================================================
    @Bean
    public KafkaMessageDrivenChannelAdapter<String, JsonNode> kafkaInboundAdapter(
            ConcurrentKafkaListenerContainerFactory<String, JsonNode> factory) {

        // Create a listener container from the shared container factory passed as argument
        var container = factory.createContainer(INPUT_TOPIC_NAME);

        KafkaMessageDrivenChannelAdapter<String, JsonNode> adapter =
                new KafkaMessageDrivenChannelAdapter<>(container, KafkaMessageDrivenChannelAdapter.ListenerMode.record);

        // Pipe the incoming payload into Spring Integration channel
        adapter.setOutputChannel(kafkaInputChannel());
        return adapter;
    }

    // =========================================================================
    // KAFKA OUTBOUND CONFIGURATION (SENDING)
    // =========================================================================
    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Dynamically mapping properties from your exact application.yml block
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerKeySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerValueSerializer);
        props.put(ProducerConfig.ACKS_CONFIG, producerAcks);
        props.put(ProducerConfig.RETRIES_CONFIG, producerRetries);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean
    @ServiceActivator(inputChannel = "kafkaOutboundChannel")
    public MessageHandler kafkaOutbound() {
        KafkaProducerMessageHandler<String, Object> handler =
                new KafkaProducerMessageHandler<>(kafkaTemplate());
        handler.setTopicExpression(new org.springframework.expression.common.LiteralExpression(OUTPUT_TOPIC_NAME));
        return handler;
    }
}
