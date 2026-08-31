package com.soaesps.notifications.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.soaesps.notifications.repository.ReactiveDisabledChannelsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.soaesps.notifications.config.IntegrationConstant.*;

/**
 * High-performance reactive Scatter-Gather (Fan-out) configuration.
 * Intercepts inbound Kafka events, evaluates disabled channels via non-blocking R2DBC,
 * and routes messages only to active, non-muted target branch channels.
 */
@Configuration
public class ReactiveScatterGatherRouter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveScatterGatherRouter.class);

    public static final String REACTIVE_ROUTER_BRIDGE = "reactiveRouterBridgeChannel";

    private final ReactiveDisabledChannelsRepository disabledChannelsRepository;

    public ReactiveScatterGatherRouter(ReactiveDisabledChannelsRepository disabledChannelsRepository) {
        this.disabledChannelsRepository = disabledChannelsRepository;
    }

    @Bean(EMAIL_BRANCH_CHANNEL)
    public MessageChannel emailBranchChannel() { return new DirectChannel(); }

    @Bean(TELEGRAM_BRANCH_CHANNEL)
    public MessageChannel telegramBranchChannel() { return new DirectChannel(); }

    @Bean(SMS_BRANCH_CHANNEL)
    public MessageChannel smsBranchChannel() { return new DirectChannel(); }

    @Bean(PUSH_BRANCH_CHANNEL)
    public MessageChannel pushBranchChannel() { return new DirectChannel(); }

    /**
     * Reactive internal bridge channel to switch standard flows to Project Reactor streams.
     */
    @Bean(REACTIVE_ROUTER_BRIDGE)
    public MessageChannel reactiveRouterBridgeChannel() {
        return new FluxMessageChannel();
    }

    /**
     * Step 1: Bridge standard channel to a reactive FluxMessageChannel
     */
    @Transformer(inputChannel = "kafkaInputChannel", outputChannel = REACTIVE_ROUTER_BRIDGE)
    public Message<JsonNode> bridgeToReactiveRouter(Message<JsonNode> message) {
        return message;
    }

    /**
     * Step 2: Asynchronously fetches blocklists from the R2DBC repository
     * and evaluates active channel routing inside a non-blocking stream context.
     */
    @Router(inputChannel = REACTIVE_ROUTER_BRIDGE)
    public Mono<List<Message<JsonNode>>> routeAllowedChannelsReactive(Message<JsonNode> message) {
        JsonNode payload = message.getPayload();
        Long userId = payload.get("userId").asLong();
        String correlationId = message.getHeaders().getId().toString(); // Use message UUID as tracking ID

        return disabledChannelsRepository.findChannelsByUserId(userId)
                .collectList()
                .map(disabledChannels -> {
                    List<String> targets = new ArrayList<>();
                    if (!disabledChannels.contains("EMAIL")) targets.add(EMAIL_BRANCH_CHANNEL);
                    if (!disabledChannels.contains("TELEGRAM")) targets.add(TELEGRAM_BRANCH_CHANNEL);
                    if (!disabledChannels.contains("SMS")) targets.add(SMS_BRANCH_CHANNEL);
                    if (!disabledChannels.contains("PUSH")) targets.add(PUSH_BRANCH_CHANNEL);

                    List<Message<JsonNode>> routedMessages = new ArrayList<>();
                    int sequenceSize = targets.size();

                    // If all channels are muted, immediately push to aggregator with size 0
                    if (sequenceSize == 0) {
                        return List.of(MessageBuilder.fromMessage(message)
                                .setHeader("targetChannel", "NONE")
                                .setHeader("IntegrationMessageHeaderAccessor.CORRELATION_ID", correlationId)
                                .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", 0)
                                .build());
                    }

                    for (int i = 0; i < sequenceSize; i++) {
                        routedMessages.add(MessageBuilder.fromMessage(message)
                                .setHeader("targetChannel", targets.get(i)) // Custom route target header
                                .setHeader("IntegrationMessageHeaderAccessor.CORRELATION_ID", correlationId)
                                .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER", i + 1)
                                .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", sequenceSize)
                                .build());
                    }
                    return routedMessages;
                });
    }
}