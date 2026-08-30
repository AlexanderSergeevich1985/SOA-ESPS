package com.soaesps.msgprocess.integration;

import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import com.soaesps.msgprocess.client.triton.TritonGrpcClient;
import com.soaesps.msgprocess.exception.MalformedFrameException;
import com.soaesps.msgprocess.service.triton.FramePreprocessor;
import com.soaesps.msgprocess.triton.InferenceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.soaesps.msgprocess.config.IntegrationConfiguration.KAFKA_INPUT_CHANNEL;
import static com.soaesps.msgprocess.config.IntegrationConfiguration.ROUTER_CHANNEL;
import static com.soaesps.msgprocess.config.RoutingConfiguration.DLQ_HEADER_VALUE;
import static com.soaesps.msgprocess.config.RoutingConfiguration.ROUTE_HEADER;

/**
 * Single-message transformer: raw Kafka byte[] → Triton inference result.
 *
 * <p>Wired into the pipeline declaratively: input from {@code kafkaInputChannel},
 * output routed either to {@code kafkaOutboundChannel} (success) or
 * {@code dlqOutboundChannel} (malformed frame / inference failure) via the router header.
 */
@Component
@Slf4j
public class InferenceTransformer {

    private final FramePreprocessor preprocessor;
    private final TritonGrpcClient triton;
    private final String modelName;
    private final String inputName;
    private final Duration timeout;

    public InferenceTransformer(
            FramePreprocessor preprocessor,
            TritonGrpcClient triton,
            @Value("${app.triton.model-name}") String modelName,
            @Value("${app.triton.input-name:INPUT__0}") String inputName,
            @Value("${app.triton.timeout-ms:5000}") long timeoutMs) {
        this.preprocessor = preprocessor;
        this.triton = triton;
        this.modelName = modelName;
        this.inputName = inputName;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    /**
     * Transforms an inbound Kafka message into an {@link InferenceResult} for the
     * success outbound channel, or a header-flagged message for the DLQ channel.
     *
     * <p>The blocking gRPC call ({@code .block()}) is acceptable here because the
     * listener container runs each record on a dedicated worker thread (see
     * {@code listener-concurrency}); the blocking does not freeze other records.
     */
    @Transformer(inputChannel = KAFKA_INPUT_CHANNEL, outputChannel = ROUTER_CHANNEL)
    public Message<?> transform(Message<MsgIOTDevice> inbound) {
        String key = (String) inbound.getHeaders().get("kafka_receivedMessageKey");
        MsgIOTDevice payload = inbound.getPayload();

        final String jsonPayload;
        try {
            jsonPayload = preprocessor.processDeviceData(inputName, payload);
        } catch (MalformedFrameException e) {
            log.warn("Malformed frame routed to DLQ: {}", e.getMessage());
            return MessageBuilder.withPayload((Object) payload)
                    .copyHeaders(inbound.getHeaders())
                    .setHeader("kafka_messageKey", key)
                    .setHeader(ROUTE_HEADER, DLQ_HEADER_VALUE)
                    .build();
        }

        try {
            InferenceResult result = triton.infer(modelName, jsonPayload)
                    .timeout(timeout)
                    .block();
            return MessageBuilder.withPayload((Object) result)
                    .copyHeaders(inbound.getHeaders())
                    .setHeader("kafka_messageKey", key)
                    .setHeader(ROUTE_HEADER, "success")
                    .build();
        } catch (Exception e) {
            log.error("Inference failed for frame with key={}, routing to DLQ", key, e);
            return MessageBuilder.withPayload((Object) payload)
                    .copyHeaders(inbound.getHeaders())
                    .setHeader("kafka_messageKey", key)
                    .setHeader(ROUTE_HEADER, DLQ_HEADER_VALUE)
                    .build();
        }
    }
}