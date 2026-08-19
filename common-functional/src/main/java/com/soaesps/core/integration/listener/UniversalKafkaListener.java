package com.soaesps.core.integration.listener;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UniversalKafkaListener {
    // Initializing standard SLF4J logger
    private static final Logger log = LoggerFactory.getLogger(UniversalKafkaListener.class);

    @KafkaListener(topics = "your-iot-topic", containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Payload JsonNode json) {

        // 1. Safe string extraction (returns empty string "" instead of null if field is missing)
        String deviceId = json.path("deviceId").asText("");

        // 2. Safe primitive extraction with fallbacks (protects against ClassCastException)
        int status = json.path("status").asInt(0);
        double temperature = json.path("metrics").path("temperature").asDouble(0.0);

        // 3. Conditional checks for optional fields
        if (json.has("criticalError")) {
            boolean isError = json.path("criticalError").asBoolean(false);
            if (isError) {
                log.warn("Device {} reported a critical error!", deviceId);
            }
        }

        // Standard structured logging using placeholders
        log.info("Received payload from device {}. Status: {}, Temperature: {}", deviceId, status, temperature);
        log.debug("Raw JSON payload structure: {}", json);
    }
}