package com.soaesps.notifications.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soaesps.notifications.config.IntegrationConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {IntegrationConfiguration.class})
@Import(IntegrationConfigurationTest.TestNotificationPipelineConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"message_queue_inbound", "message_queue_outbound"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("Notifications Microservice Integration Pipeline Test")
public class IntegrationConfigurationTest {

    // Системные свойства для инициализации бинов продюсера в вашей конфигурации
    static {
        System.setProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        System.setProperty("spring.kafka.producer.acks", "1");
        System.setProperty("spring.kafka.producer.retries", "0");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MessageChannel pubSubFileChannel;

    @Autowired
    private TestNotificationPipelineConfig.DirectChannelInterceptor inputChannelInterceptor;

    @Autowired
    private TestNotificationPipelineConfig.KafkaTestOutboundListener outboundListener;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void clearQueues() {
        inputChannelInterceptor.getReceivedMessages().clear();
        outboundListener.getOutboundQueue().clear();
    }

    @Test
    @DisplayName("Inbound Adapter should receive record from Kafka and pass it to kafkaInputChannel")
    void shouldReceiveInboundKafkaMessage() throws InterruptedException {
        // Arrange
        ObjectNode jsonPayload = objectMapper.createObjectNode();
        jsonPayload.put("notificationId", "notif-123");
        jsonPayload.put("status", "PENDING");

        // Act - отправляем во входящий топик адаптера
        kafkaTemplate.send("message_queue_inbound", "key-in", jsonPayload);

        // Assert - проверяем, что адаптер перехватил сообщение и отправил в Spring Integration канал
        org.springframework.messaging.Message<?> receivedInChannel =
                inputChannelInterceptor.getReceivedMessages().poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedInChannel, "Message didn't reach kafkaInputChannel");
        JsonNode payloadNode = (JsonNode) receivedInChannel.getPayload();
        assertEquals("notif-123", payloadNode.get("notificationId").asText());
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
        ConsumerRecord<String, JsonNode> kafkaRecord = outboundListener.getOutboundQueue().poll(5, TimeUnit.SECONDS);

        assertNotNull(kafkaRecord, "Message was not received in message_queue_outbound topic");
        assertEquals("res-999", kafkaRecord.value().get("resultId").asText());
    }

    @TestConfiguration
    static class TestNotificationPipelineConfig {

        // Перехватчик для проверки сообщений внутри DirectChannel (kafkaInputChannel)
        @Bean
        public DirectChannelInterceptor kafkaInputChannelInterceptor(MessageChannel kafkaInputChannel) {
            DirectChannelInterceptor interceptor = new DirectChannelInterceptor();
            ((DirectChannel) kafkaInputChannel).addInterceptor(interceptor);
            return interceptor;
        }

        // Инфраструктура для тестирования отправки: создаем Listener контейнер фабрику для тестового слушателя
        @Bean(name = "testOutboundListenerContainerFactory")
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

        @Bean
        public KafkaTestOutboundListener kafkaTestOutboundListener() {
            return new KafkaTestOutboundListener();
        }

        // Вспомогательный перехватчик каналов Spring Integration
        static class DirectChannelInterceptor implements org.springframework.messaging.support.ChannelInterceptor {
            private final BlockingQueue<org.springframework.messaging.Message<?>> receivedMessages = new LinkedBlockingQueue<>();

            @Override
            public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, MessageChannel channel) {
                receivedMessages.add(message);
                return message;
            }

            public BlockingQueue<org.springframework.messaging.Message<?>> getReceivedMessages() {
                return receivedMessages;
            }
        }

        // Тестовый слушатель выходного топика
        static class KafkaTestOutboundListener {
            private final BlockingQueue<ConsumerRecord<String, JsonNode>> outboundQueue = new LinkedBlockingQueue<>();

            @KafkaListener(topics = "message_queue_outbound", groupId = "test-outbound-group",
                    containerFactory = "testOutboundListenerContainerFactory")
            public void listenOutbound(ConsumerRecord<String, JsonNode> record) {
                outboundQueue.add(record);
            }

            public BlockingQueue<ConsumerRecord<String, JsonNode>> getOutboundQueue() {
                return outboundQueue;
            }
        }
    }
}