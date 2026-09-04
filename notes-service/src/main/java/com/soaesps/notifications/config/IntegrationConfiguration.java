package com.soaesps.notifications.config;

import com.soaesps.core.config.BaseKafkaConsumerConfig;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.component.aggregator.CorrelationStrategyI;
import com.soaesps.core.component.aggregator.ReleaseStrategyI;
import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.component.InboundNotificationFilter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.integration.filter.MessageFilter;
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

import static com.soaesps.notifications.config.IntegrationConstant.INPUT_TOPIC_NAME;
import static com.soaesps.notifications.config.IntegrationConstant.OUTPUT_TOPIC_NAME;

@Configuration
@ComponentScan("com.soaesps.notifications.integration")
@EnableIntegration
@IntegrationComponentScan("com.soaesps.notifications.integration")
@Import(BaseKafkaConsumerConfig.class) // Imports custom Kafka consumer beans
public class IntegrationConfiguration {

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

    // =========================================================================
    // CORE CORE FLOW CHANNELS
    // =========================================================================

    @Bean(IntegrationConstant.KAFKA_INPUT_CHANNEL)
    public MessageChannel kafkaInputChannel() {
        return new DirectChannel();
    }

    @Bean(IntegrationConstant.DISCARD_FILTER_CHANNEL)
    public MessageChannel discardFilterChannel() {
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

    @Filter(
            inputChannel = IntegrationConstant.KAFKA_INPUT_CHANNEL,
            outputChannel = IntegrationConstant.REACTIVE_ROUTER_BRIDGE//, // Next step: reactive stream bridge
            //discardChannel = IntegrationConstant.DISCARD_FILTER_CHANNEL // If type is invalid/unsupported
    )
    @Bean
    public MessageFilter inboundTypeFilter(InboundNotificationFilter filter) {
        // Spring Integration core filter component wrapping reactive checker
        MessageFilter messageFilter = new MessageFilter(
                message -> filter.isNotificationTypeSupported((InboundNotificationEvent) message.getPayload())
        );

        // Explicitly set the discard channel programmatically via the setter as required by the framework
        messageFilter.setDiscardChannelName(IntegrationConstant.DISCARD_FILTER_CHANNEL);

        return messageFilter;
    }

    // =========================================================================
    // AGGREGATION & STRATEGIES
    // =========================================================================

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
    public KafkaMessageDrivenChannelAdapter<String, InboundNotificationEvent> kafkaInboundAdapter(
            ConcurrentKafkaListenerContainerFactory<String, InboundNotificationEvent> inNotesKafkaListenerContainerFactory) {

        var container = inNotesKafkaListenerContainerFactory.createContainer(INPUT_TOPIC_NAME);
        var adapter = new KafkaMessageDrivenChannelAdapter<>(container, KafkaMessageDrivenChannelAdapter.ListenerMode.record);

        adapter.setOutputChannel(kafkaInputChannel());
        return adapter;
    }

    // =========================================================================
    // KAFKA OUTBOUND CONFIGURATION (SENDING)
    // =========================================================================

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        Map<String, Object> props = new HashMap<>();
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
        var handler = new KafkaProducerMessageHandler<>(kafkaTemplate());
        handler.setTopicExpression(new LiteralExpression(OUTPUT_TOPIC_NAME));
        return handler;
    }
}