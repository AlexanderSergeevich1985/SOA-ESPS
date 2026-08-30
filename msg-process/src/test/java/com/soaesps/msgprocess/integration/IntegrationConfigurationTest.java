package com.soaesps.msgprocess.integration;

import com.soaesps.msgprocess.DataModels.message.MsgBody;
import com.soaesps.msgprocess.DataModels.message.MsgHeader;
import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import com.soaesps.msgprocess.config.IntegrationConfiguration;
import com.soaesps.msgprocess.config.KafkaConsumerConfig;
import com.soaesps.msgprocess.config.RoutingConfiguration;
import com.soaesps.msgprocess.triton.InferenceResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.soaesps.msgprocess.config.IntegrationConfiguration.*;
import static com.soaesps.msgprocess.config.KafkaConsumerConfig.IOT_KAFKA_CONTAINER;
import static com.soaesps.msgprocess.config.RoutingConfiguration.DLQ_HEADER_VALUE;
import static com.soaesps.msgprocess.config.RoutingConfiguration.ROUTE_HEADER;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {IntegrationConfiguration.class, KafkaConsumerConfig.class, RoutingConfiguration.class})
@Import(IntegrationConfigurationTest.TestPipelineConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME, DLQ_TOPIC_NAME},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@DisplayName("Integration Pipeline Configuration Test")
public class IntegrationConfigurationTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
        System.setProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        System.setProperty("spring.kafka.producer.acks", "1");
        System.setProperty("spring.kafka.producer.retries", "0");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestPipelineConfig.KafkaTestListener testListener;

    @BeforeEach
    void clearQueues() {
        testListener.getResultsQueue().clear();
        testListener.getDlqQueue().clear();
    }

    @Test
    @DisplayName("Pipeline should route successful frame to inference-results topic")
    void shouldRouteSuccessfulFrameToResultsTopic() throws InterruptedException {
        // Arrange: Populate valid Header and Body to prevent NullPointerException
        MsgHeader header = new MsgHeader();
        header.setMessageId("msg-success-01"); // Normal ID for regular pipeline processing

        MsgBody body = new MsgBody();
        MsgIOTDevice payload = new MsgIOTDevice(header, body);

        // Act: Send message and flush to ensure it reaches the embedded broker immediately
        kafkaTemplate.send(INPUT_TOPIC_NAME, "key-1", payload);
        kafkaTemplate.flush();

        // Assert: Poll for the expected InferenceResult (increased timeout for CI stability)
        ConsumerRecord<String, InferenceResult> received = testListener.getResultsQueue().poll(10, TimeUnit.SECONDS);
        assertNotNull(received, "Message was not received in inference-results topic");
    }

    @Test
    @DisplayName("Pipeline should route malformed frame to frames-dlq topic")
    void shouldRouteMalformedFrameToDlqTopic() throws InterruptedException {
        // Arrange: Populate header with a specific keyword to trigger DLQ routing condition
        MsgHeader header = new MsgHeader();
        header.setMessageId("msg-failed-02"); // Contains "failed" marker for routing logic

        MsgBody body = new MsgBody();
        MsgIOTDevice payload = new MsgIOTDevice(header, body);

        // Act: Send message and flush
        kafkaTemplate.send(INPUT_TOPIC_NAME, "key-2", payload);
        kafkaTemplate.flush();

        // Assert: Poll for the original MsgIOTDevice in DLQ
        ConsumerRecord<String, MsgIOTDevice> received = testListener.getDlqQueue().poll(10, TimeUnit.SECONDS);
        assertNotNull(received, "Message was not received in frames-dlq topic");
    }

    @TestConfiguration
    static class TestPipelineConfig {

        @Value("${spring.kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
        private String consumerAutoOffsetReset;

        public static final String INFERENCE_KAFKA_CONTAINER = "tensorKafkaListenerContainerFactory";
        public static final String TEST_GROUP_ID = "test-results-group";
        public static final String TEST_DLQ_GROUP_ID = "test-dlq-group";

        // =========================================================================
        // INFERENCE RESULTS CONSUMER (Success Path)
        // =========================================================================

        @Bean(name = INFERENCE_KAFKA_CONTAINER)
        public ConcurrentKafkaListenerContainerFactory<String, InferenceResult> tensorKafkaListenerContainerFactory(
                ConsumerFactory<String, InferenceResult> tensorConsumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, InferenceResult> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(tensorConsumerFactory);
            return factory;
        }

        @Bean
        public ConsumerFactory<String, InferenceResult> tensorConsumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = new HashMap<>();

            // Use injected properties
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, TEST_GROUP_ID);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);

            // Safe key deserialization via ErrorHandlingDeserializer
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

            // Safe value deserialization (InferenceResult)
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

            // Configure JsonDeserializer for proper handling of Java classes/records
            JsonDeserializer<InferenceResult> jsonDeserializer = new JsonDeserializer<>(InferenceResult.class);
            jsonDeserializer.addTrustedPackages("*"); // Allow deserialization of all packages into this class

            // Pass StringDeserializer for keys and the configured jsonDeserializer for values
            return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
        }

        // =========================================================================
        // IOT DEVICE CONSUMER (DLQ Path)
        // =========================================================================

        /*@Bean(name = IOT_KAFKA_CONTAINER)
        public ConcurrentKafkaListenerContainerFactory<String, MsgIOTDevice> iotKafkaListenerContainerFactory(
                ConsumerFactory<String, MsgIOTDevice> iotConsumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, MsgIOTDevice> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(iotConsumerFactory);
            return factory;
        }

        @Bean
        public ConsumerFactory<String, MsgIOTDevice> iotConsumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = new HashMap<>();

            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, TEST_DLQ_GROUP_ID);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);

            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

            JsonDeserializer<MsgIOTDevice> jsonDeserializer = new JsonDeserializer<>(MsgIOTDevice.class);
            jsonDeserializer.addTrustedPackages("*");

            return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
        }*/

        // =========================================================================
        // MOCK TRANSFORMER & LISTENERS
        // =========================================================================

        @Bean
        public MockTransformer mockTransformer() {
            return new MockTransformer();
        }

        @Bean
        public KafkaTestListener kafkaTestListener() {
            return new KafkaTestListener();
        }

        static class MockTransformer {
            @Transformer(inputChannel = KAFKA_INPUT_CHANNEL, outputChannel = ROUTER_CHANNEL)
            public Message<?> transform(Message<MsgIOTDevice> inputMessage) {
                MsgIOTDevice payload = inputMessage.getPayload();

                // Inspecting the messageId from header to determine routing path safely
                if (payload.getHeader() != null && payload.getHeader().getMessageId() != null
                        && payload.getHeader().getMessageId().contains("failed")) {

                    // Route directly to Dead Letter Queue channel
                    return MessageBuilder.withPayload(payload)
                            .copyHeaders(inputMessage.getHeaders())
                            .setHeader(ROUTE_HEADER, DLQ_HEADER_VALUE)
                            .build();
                }

                InferenceResult result = new InferenceResult(payload.getHeader().getMessageId(), new ArrayList<>(), new ArrayList<>());

                // Regular outbound success path routing
                return MessageBuilder.withPayload(result)
                        .copyHeaders(inputMessage.getHeaders())
                        .build();
            }
        }

        static class KafkaTestListener {

            // FIX: Changed type to InferenceResult to match the success path payload
            private final BlockingQueue<ConsumerRecord<String, InferenceResult>> resultsQueue = new LinkedBlockingQueue<>();
            private final BlockingQueue<ConsumerRecord<String, MsgIOTDevice>> dlqQueue = new LinkedBlockingQueue<>();

            @KafkaListener(topics = OUTPUT_TOPIC_NAME, groupId = TEST_GROUP_ID,
                    containerFactory = INFERENCE_KAFKA_CONTAINER)
            public void listenResults(ConsumerRecord<String, InferenceResult> record) {
                resultsQueue.add(record);
            }

            @KafkaListener(topics = DLQ_TOPIC_NAME, groupId = TEST_DLQ_GROUP_ID,
                    containerFactory = IOT_KAFKA_CONTAINER)
            public void listenDlq(ConsumerRecord<String, MsgIOTDevice> record) {
                dlqQueue.add(record);
            }

            public BlockingQueue<ConsumerRecord<String, InferenceResult>> getResultsQueue() {
                return resultsQueue;
            }

            public BlockingQueue<ConsumerRecord<String, MsgIOTDevice>> getDlqQueue() {
                return dlqQueue;
            }
        }
    }
}