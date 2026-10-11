package com.soaesps.notifications.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soaesps.notifications.channel.EmailNotificationChannel;
import com.soaesps.notifications.component.InboundNotificationFilter;
import com.soaesps.notifications.handler.SmsBranchHandler;
import com.soaesps.notifications.config.*;
import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.repository.reactive.ReactiveContactRepository;
import com.soaesps.notifications.repository.reactive.ReactiveDisabledChannelsRepository;
import com.soaesps.notifications.repository.reactive.ReactiveNotificationTypeRepository;
import com.soaesps.notifications.service.render.ReactiveHtmlRenderService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.soaesps.notifications.config.IntegrationConstant.DLQ_TOPIC_NAME;
import static com.soaesps.notifications.config.IntegrationConstant.OUTPUT_TOPIC_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        IntegrationConfiguration.class,
        KafkaConsumerConfig.class,
        InboundNotificationFilter.class,
        ReactiveScatterGatherRouter.class,
        ReactiveHtmlRenderService.class,
        SmsBranchHandler.class,
        NotificationStatusAggregator.class
})
@ImportAutoConfiguration({
        R2dbcAutoConfiguration.class,
        R2dbcDataAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password=",
        "spring.sql.init.mode=always"   // applies schema.sql via ConnectionFactory
})
//@Import(RealPipelineIntegrationTest.TestPipelineConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {IntegrationConstant.KAFKA_INPUT_CHANNEL, IntegrationConstant.KAFKA_OUTPUT_CHANNEL},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@DisplayName("Notifications Microservice Integration Pipeline Test")
@EnableR2dbcRepositories(basePackages = "com.soaesps.notifications.repository.reactive")

public class IntegrationConfigurationTest {

    // Системные свойства для инициализации бинов продюсера в вашей конфигурации
    static {
        System.setProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        System.setProperty("spring.kafka.producer.acks", "1");
        System.setProperty("spring.kafka.producer.retries", "0");
    }

    @MockitoBean
    private ReactiveDisabledChannelsRepository reactiveDisabledChannelsRepository;

    @MockitoBean
    private ReactiveContactRepository reactiveContactRepository;

    @MockitoBean
    ReactiveNotificationTypeRepository reactiveNotificationTypeRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MessageChannel pubSubFileChannel;

    @Autowired
    private TestPipelineConfig.DirectChannelInterceptor inputChannelInterceptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmailNotificationChannel emailSender;

    @Autowired
    private TestPipelineConfig.KafkaTestListener testListener;

    @BeforeEach
    void setUpMocksAndQueues() {
        Mockito.when(reactiveNotificationTypeRepository.findAllActiveTypes())
                .thenReturn(Flux.just("PAYMENT_SUCCESS"));
        inputChannelInterceptor.getReceivedMessages().clear();
        testListener.getResultsQueue().clear();
        testListener.getDlqQueue().clear();
    }

    @Test
    @DisplayName("Inbound Adapter should receive record from Kafka and pass it to kafkaInputChannel")
    void shouldReceiveInboundKafkaMessage() throws InterruptedException {
        // Arrange
        ObjectNode jsonPayload = objectMapper.createObjectNode();
        jsonPayload.put("notificationId", "notif-123");
        jsonPayload.put("userId", 777L);
        jsonPayload.put("notificationType", "PAYMENT_SUCCESS");

        // Act - отправляем во входящий топик адаптера
        kafkaTemplate.send("message_queue_inbound", "key-in", jsonPayload);

        // Assert - проверяем, что адаптер перехватил сообщение и отправил в Spring Integration канал
        Message<?> receivedInChannel =
                inputChannelInterceptor.getReceivedMessages().poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedInChannel, "Message didn't reach kafkaInputChannel");
        InboundNotificationEvent payloadNode = (InboundNotificationEvent) receivedInChannel.getPayload();
        assertEquals("notif-123", payloadNode.notificationId());
    }

    @Test
    @DisplayName("Outbound Message should flow through PubSub and Bridge to output Kafka topic")
    void shouldSendOutboundKafkaMessageViaPubSubBridge() throws InterruptedException {
        // Arrange
        ObjectNode jsonPayload = objectMapper.createObjectNode();
        jsonPayload.put("resultId", "res-999");
        jsonPayload.put("delivery", "SUCCESS");

        org.springframework.messaging.Message<JsonNode> siMessage = MessageBuilder.withPayload((JsonNode) jsonPayload)
                .setHeader("test-header", "secured")
                .build();

        // Act - отправляем в цепочку через начальный PubSub канал
        pubSubFileChannel.send(siMessage);

        // Assert - проверяем, что благодаря Bridge сообщение дошло до хэндлера и улетело в Kafka топик аутбаунда
        /*ConsumerRecord<String, JsonNode> kafkaRecord = outboundListener.getOutboundQueue().poll(5, TimeUnit.SECONDS);

        assertNotNull(kafkaRecord, "Message was not received in message_queue_outbound topic");
        assertEquals("res-999", kafkaRecord.value().get("resultId").asText());*/
    }

    @TestConfiguration
    static class TestPipelineConfig {
        public static final String NOTIFICATION_KAFKA_CONTAINER = "notesKafkaListenerContainerFactory";
        public static final String TEST_GROUP_ID = "test-results-group";
        public static final String TEST_DLQ_GROUP_ID = "test-dlq-group";

        @Value("${spring.kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
        private String consumerAutoOffsetReset;

        @Bean
        public KafkaTestListener kafkaTestListener() {
            return new KafkaTestListener();
        }

        @Bean(name = NOTIFICATION_KAFKA_CONTAINER)
        public ConcurrentKafkaListenerContainerFactory<String, ObjectNode> testNotesKafkaListenerContainerFactory(
                ConsumerFactory<String, ObjectNode> notesConsumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, ObjectNode> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(notesConsumerFactory);
            return factory;
        }

        @Bean
        public ConsumerFactory<String, ObjectNode> tensorConsumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = new HashMap<>();

            // Use injected properties
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, TEST_GROUP_ID);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);

            // Safe key deserialization via ErrorHandlingDeserializer
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

            // Safe value deserialization (ObjectNode)
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

            // Configure JsonDeserializer for proper handling of Java classes/records
            JsonDeserializer<ObjectNode> jsonDeserializer = new JsonDeserializer<>(ObjectNode.class);
            jsonDeserializer.addTrustedPackages("*"); // Allow deserialization of all packages into this class

            // Pass StringDeserializer for keys and the configured jsonDeserializer for values
            return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
        }

        // Перехватчик для проверки сообщений внутри DirectChannel (kafkaInputChannel)
        @Bean
        public DirectChannelInterceptor kafkaInputChannelInterceptor(MessageChannel kafkaInputChannel) {
            DirectChannelInterceptor interceptor = new DirectChannelInterceptor();
            ((DirectChannel) kafkaInputChannel).addInterceptor(interceptor);
            return interceptor;
        }

        // Инфраструктура для тестирования отправки: создаем Listener контейнер фабрику для тестового слушателя
        //@Bean(name = "testOutboundListenerContainerFactory")
        public ConcurrentKafkaListenerContainerFactory<String, JsonNode> testOutboundListenerContainerFactory(
                EmbeddedKafkaBroker embeddedKafkaBroker) {

            Map<String, Object> configs = KafkaTestUtils.consumerProps("test-notification-group", "true", embeddedKafkaBroker);
            configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            ConsumerFactory<String, JsonNode> consumerFactory = new DefaultKafkaConsumerFactory<>(
                    configs,
                    new StringDeserializer(),
                    new JsonDeserializer<>(JsonNode.class)
            );

            ConcurrentKafkaListenerContainerFactory<String, JsonNode> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }

        // Вспомогательный перехватчик каналов Spring Integration
        static class DirectChannelInterceptor implements ChannelInterceptor {
            private final BlockingQueue<Message<?>> receivedMessages = new LinkedBlockingQueue<>();

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                receivedMessages.add(message);
                return message;
            }

            public BlockingQueue<Message<?>> getReceivedMessages() {
                return receivedMessages;
            }
        }

        // Тестовый слушатель выходного топика
        static class KafkaTestOutboundListener {
            private final BlockingQueue<ConsumerRecord<String, JsonNode>> outboundQueue = new LinkedBlockingQueue<>();

            @KafkaListener(topics = IntegrationConstant.KAFKA_OUTPUT_CHANNEL, groupId = "test-outbound-group",
                    containerFactory = "testOutboundListenerContainerFactory")
            public void listenOutbound(ConsumerRecord<String, JsonNode> record) {
                outboundQueue.add(record);
            }

            public BlockingQueue<ConsumerRecord<String, JsonNode>> getOutboundQueue() {
                return outboundQueue;
            }
        }

        static class KafkaTestListener {
            private final BlockingQueue<ConsumerRecord<String, ObjectNode>> resultsQueue = new LinkedBlockingQueue<>();
            private final BlockingQueue<ConsumerRecord<String, JsonNode>> dlqQueue = new LinkedBlockingQueue<>();

            @KafkaListener(topics = OUTPUT_TOPIC_NAME, groupId = TEST_GROUP_ID,
                    containerFactory = NOTIFICATION_KAFKA_CONTAINER)
            public void listenResults(ConsumerRecord<String, ObjectNode> record) {
                resultsQueue.add(record);
            }

            @KafkaListener(topics = DLQ_TOPIC_NAME, groupId = TEST_DLQ_GROUP_ID,
                    containerFactory = NOTIFICATION_KAFKA_CONTAINER)
            public void listenDlq(ConsumerRecord<String, JsonNode> record) {
                dlqQueue.add(record);
            }

            public BlockingQueue<ConsumerRecord<String, ObjectNode>> getResultsQueue() {
                return resultsQueue;
            }

            public BlockingQueue<ConsumerRecord<String, JsonNode>> getDlqQueue() {
                return dlqQueue;
            }
        }
    }
}