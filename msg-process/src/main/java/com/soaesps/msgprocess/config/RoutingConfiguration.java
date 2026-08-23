package com.soaesps.msgprocess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.transformer.HeaderFilter;
import org.springframework.messaging.Message;

/**
 * Routes messages from the transformer output to either the success outbound channel
 * or the DLQ, based on the {@code route} header set by {@code InferenceTransformer}.
 */
@Configuration
public class RoutingConfiguration {

    @Router(inputChannel = "routingChannel")
    public String route(Message<?> message) {
        Object route = message.getHeaders().get("route");
        return "dlq".equals(route)
                ? IntegrationConfiguration.DLQ_OUTBOUND_CHANNEL
                : IntegrationConfiguration.KAFKA_OUTBOUND_CHANNEL;
    }

    /**
     * Strips the transient routing header before the message reaches the producer handler,
     * so only business headers survive to Kafka.
     */
    @Bean
    public HeaderFilter headerFilter() {
        return new HeaderFilter("route");
    }
}