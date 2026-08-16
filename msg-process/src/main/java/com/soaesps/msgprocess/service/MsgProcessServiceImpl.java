package com.soaesps.msgprocess.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import com.soaesps.msgprocess.DataModels.message.MsgResult;
import com.soaesps.msgprocess.Utils.BaseQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service implementation for processing IoT device messages via Kafka and WebSocket.
 */
@Service
public class MsgProcessServiceImpl implements MsgProcessService {
    private static final Logger logger = LoggerFactory.getLogger(MsgProcessServiceImpl.class);

    private final KafkaTemplate<String, MsgIOTDevice> kafkaTemplate;
    private final SimpMessagingTemplate smsgt;
    private final ObjectMapper objectMapper;

    public MsgProcessServiceImpl(KafkaTemplate<String, MsgIOTDevice> kafkaTemplate,
                                 SimpMessagingTemplate smsgt,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.smsgt = smsgt;
        this.objectMapper = objectMapper;
    }

    private final BaseQueue<MsgResult> msgQueue = new BaseQueue<>();

    public MsgResult process(final String topicName, final MsgIOTDevice msg) {
        CompletableFuture<SendResult<String, MsgIOTDevice>> future = kafkaTemplate.send(topicName, msg);

        // Optional: Add callback for async error handling
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error("Failed to send message to topic {}: {}", topicName, ex.getMessage());
            } else {
                logger.debug("Message sent to topic {} with offset {}",
                        topicName, result.getRecordMetadata().offset());
            }
        });

        // WARNING: This synchronous pull after async send is an anti-pattern.
        // Consider using CompletableFuture to properly wait for the response.
        return msgQueue.pull();
    }

    public void send(final String topicName, final MsgResult msg) {
        smsgt.convertAndSend(topicName, msg);
    }

    @KafkaListener(topics = "${spring.kafka.topic.result}", groupId = "${spring.kafka.group-id}")
    public void consume(@Payload String message) {
        try {
            MsgResult result = objectMapper.readValue(message, MsgResult.class);
            msgQueue.push(result);
            logger.debug("Consumed message from Kafka: {}", result);
        } catch (Exception e) {
            logger.error("Failed to deserialize Kafka message: {}", message, e);
        }
    }
}