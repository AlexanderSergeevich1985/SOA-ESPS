package com.soaesps.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.config.BaseKafkaConsumerConfig;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.component.aggregator.CorrelationStrategyI;
import com.soaesps.core.component.aggregator.ReleaseStrategyI;
import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.component.InboundNotificationFilter;
import com.soaesps.notifications.grpc.notification.NotificationRequest;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.dsl.IntegrationFlow;
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
import java.util.UUID;

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

    @Autowired
    private ObjectMapper mapper;

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

    // =========================================================================
    // GRPC INBOUND CONFIGURATION (RECEIVING)
    // =========================================================================

    // Channel for incoming gRPC requests
    @Bean(IntegrationConstant.GRPC_INPUT_CHANNEL)
    public MessageChannel grpcNotificationInputChannel() {
        return new DirectChannel(); // Use ExecutorChannel if asynchronous input is required
    }

    /*@Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        return new AnonymousAuthenticationReader("anonymous_grpc_key");
    }*/

    @Bean
    public IntegrationFlow grpcInboundFlow(InboundNotificationFilter filter) {
        return IntegrationFlow.from(IntegrationConstant.GRPC_INPUT_CHANNEL)
                .transform(this::mapGrpcToInboundEvent)
                .filter(
                        filter::isNotificationTypeSupported,
                        c -> c.discardChannel(IntegrationConstant.DISCARD_FILTER_CHANNEL)
                )
                .channel(IntegrationConstant.REACTIVE_ROUTER_BRIDGE)
                .get();
    }

    private InboundNotificationEvent mapGrpcToInboundEvent(Object payload) {
        //log.info("Starting mapping execution for incoming gRPC payload");
        try {
            // Extract gRPC request even if wrapped in a Spring Integration Message container
            NotificationRequest request = getNotificationRequest(payload);

            // Extract and parse the nested raw JSON string into a dynamic parameters Map
            String jsonPayload = request.getPayloadJson();
            Map<String, Object> parsedParameters = new HashMap<>();

            if (!jsonPayload.isEmpty() && !jsonPayload.equals("{}")) {
                // Safely deserialize dynamic JSON into a Map, matching the record's "parameters" type
                parsedParameters = mapper.readValue(
                        jsonPayload,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
                );
            }

            // Construct the immutable InboundNotificationEvent record using gRPC metadata

            //log.info("Mapping execution successfully finished for event type: {}", event.notificationType());
            return new InboundNotificationEvent(
                    UUID.randomUUID().toString(),            // notificationId
                    null,                                              // userId (can be parsed from recipient if needed)
                    request.getTemplateName(),                         // notificationType (mapped from templateName)
                    parsedParameters                                   // parameters (dynamic data for templates)
            );
        } catch (Exception e) {
            //log.error("Critical mapping failure occurred inside mapGrpcToInboundEvent processing node!", e);
            throw new org.springframework.messaging.MessagingException("Pipeline transformation mapping error: " + e.getMessage(), e);
        }
    }

    private static @NotNull NotificationRequest getNotificationRequest(Object payload) {
        NotificationRequest request;
        if (payload instanceof Message) {
            request = (NotificationRequest) ((Message<?>) payload).getPayload();
        } else if (payload instanceof NotificationRequest) {
            request = (NotificationRequest) payload;
        } else {
            throw new IllegalArgumentException("Unsupported payload type in integration pipeline: " + payload.getClass().getName());
        }
        return request;
    }
}