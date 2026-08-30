package com.soaesps.msgprocess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.transformer.HeaderFilter;
import org.springframework.messaging.Message;

import static com.soaesps.msgprocess.config.IntegrationConfiguration.ROUTER_CHANNEL;
import static org.springframework.messaging.MessageHeaders.ERROR_CHANNEL;
import static org.springframework.messaging.MessageHeaders.REPLY_CHANNEL;

/**
 * Routes messages from the transformer output to either the success outbound channel
 * or the DLQ, based on the {@code route} header set by {@code InferenceTransformer}.
 */
@Configuration
public class RoutingConfiguration {
    public static final String HEAD_FILTER_CHANNEL = "kafkaPreFilterChannel";
    public static final String ROUTE_HEADER = "route";
    public static final String DLQ_HEADER_VALUE = "dlq";

    @Router(inputChannel = ROUTER_CHANNEL)
    public String route(Message<?> message) {
        Object route = message.getHeaders().get(ROUTE_HEADER);
        return DLQ_HEADER_VALUE.equals(route)
                ? IntegrationConfiguration.DLQ_OUTBOUND_CHANNEL
                : HEAD_FILTER_CHANNEL;
    }

    /**
     * Strips the transient routing header before the message reaches the producer handler,
     * so only business headers survive to Kafka.
     */
    @Bean
    @Transformer(inputChannel = HEAD_FILTER_CHANNEL, outputChannel = IntegrationConfiguration.KAFKA_OUTBOUND_CHANNEL)
    public HeaderFilter headerFilter() {
        return new HeaderFilter(ROUTE_HEADER, REPLY_CHANNEL, ERROR_CHANNEL);
    }
}