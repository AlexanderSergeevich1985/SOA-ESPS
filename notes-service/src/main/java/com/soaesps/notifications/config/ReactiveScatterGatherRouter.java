package com.soaesps.notifications.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.soaesps.notifications.domain.reactive.EmailContactRow;
import com.soaesps.notifications.domain.reactive.PushContactRow;
import com.soaesps.notifications.domain.reactive.SmsContactRow;
import com.soaesps.notifications.domain.reactive.TelegramContactRow;
import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import com.soaesps.notifications.repository.reactive.*;
import com.soaesps.notifications.service.render.ReactiveHtmlRenderService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.soaesps.notifications.config.IntegrationConstant.*;

/**
 * High-performance reactive Scatter-Gather (Fan-out) configuration.
 * Intercepts inbound Kafka events, evaluates disabled channels via non-blocking R2DBC,
 * and routes messages only to active, non-muted target branch channels.
 */
@Configuration
public class ReactiveScatterGatherRouter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveScatterGatherRouter.class);

    private final ReactiveDisabledChannelsRepository disabledChannelsRepository;

    private final ReactiveEmailContactRepository emailContactRepository;
    private final ReactiveSmsContactRepository smsContactRepository;
    private final ReactivePushContactRepository pushContactRepository;
    private final ReactiveTelegramContactRepository telegramContactRepository;

    private final ReactiveHtmlRenderService templateEngine;


    public ReactiveScatterGatherRouter(ReactiveDisabledChannelsRepository disabledChannelsRepository,
                                       ReactiveEmailContactRepository emailContactRepository,
                                       ReactiveSmsContactRepository smsContactRepository,
                                       ReactivePushContactRepository pushContactRepository,
                                       ReactiveTelegramContactRepository telegramContactRepository,
                                       ReactiveHtmlRenderService templateEngine) {
        this.disabledChannelsRepository = disabledChannelsRepository;
        this.emailContactRepository = emailContactRepository;
        this.smsContactRepository = smsContactRepository;
        this.pushContactRepository = pushContactRepository;
        this.telegramContactRepository = telegramContactRepository;
        this.templateEngine = templateEngine;
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
    @Transformer(inputChannel = KAFKA_INPUT_CHANNEL, outputChannel = REACTIVE_ROUTER_BRIDGE)
    public Message<JsonNode> bridgeToReactiveRouter(Message<JsonNode> message) {
        return message;
    }

    /**
     * Step 2: Asynchronously fetches blocklists from the R2DBC repository
     * and evaluates active channel routing inside a non-blocking stream context.
     */
    @Router(inputChannel = REACTIVE_ROUTER_BRIDGE)
    public Mono<List<Message<OutboundRoutingEnvelope>>> routeAllowedChannelsReactive(Message<InboundNotificationEvent> message) {
        InboundNotificationEvent event = message.getPayload();
        Long userId = event.userId();
        String correlationId = Objects.requireNonNull(message.getHeaders().getId()).toString(); // Use message UUID as tracking ID

        return disabledChannelsRepository.findChannelsByUserId(userId)
                .collectList()
                .flatMap(disabledChannels -> {
                    List<String> targetChannels = new ArrayList<>();
                    if (!disabledChannels.contains("EMAIL")) targetChannels.add(EMAIL_BRANCH_CHANNEL);
                    if (!disabledChannels.contains("TELEGRAM")) targetChannels.add(TELEGRAM_BRANCH_CHANNEL);
                    if (!disabledChannels.contains("SMS")) targetChannels.add(SMS_BRANCH_CHANNEL);
                    if (!disabledChannels.contains("PUSH")) targetChannels.add(PUSH_BRANCH_CHANNEL);

                    int sequenceSize = targetChannels.size();

                    if (sequenceSize == 0) {
                        log.warn("All messaging channels are muted for userId: {}. Terminal drop route triggered.", userId);
                        var mutedEnvelope = new OutboundRoutingEnvelope(userId, "NONE", List.of("NONE"), List.of(Map.of()), "MUTED", "MUTED");
                        return Mono.just(List.of(MessageBuilder.withPayload(mutedEnvelope)
                                .setHeader("targetChannel", "NONE")
                                .setHeader("IntegrationMessageHeaderAccessor.CORRELATION_ID", correlationId)
                                .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", 0)
                                .build()));
                    }

                    List<Mono<Message<OutboundRoutingEnvelope>>> messageMonos = new ArrayList<>();

                    for (int i = 0; i < sequenceSize; i++) {
                        String physicalChannelName = targetChannels.get(i);
                        String channelTypeKey = physicalChannelName.replace("BranchChannel", "").toUpperCase();
                        int sequenceNumber = i + 1;

                        // Trigger template engine to render title and body exactly ONCE per channel type boundary
                        Mono<ReactiveHtmlRenderService.RenderedContent> contentRenderMono = templateEngine.renderAsync(channelTypeKey, event).cache();

                        // Fetch ALL active contact records and collect them as a unified list array container
                        Mono<Message<OutboundRoutingEnvelope>> channelPipelineMono = resolveTargetEnvelopes(userId, channelTypeKey)
                                .collectList()
                                .flatMap(targetPairs -> contentRenderMono.map(renderedContent -> {

                                    // Split target pairs into separate parallel list structures matching DTO schema definition bounds
                                    List<String> addressList = targetPairs.stream().map(TargetContactPair::destinationValue).toList();
                                    List<Map<String, String>> metaList = targetPairs.stream().map(TargetContactPair::meta).toList();

                                    // If no active endpoints exist for this user in DB, use a fallback tracer string
                                    if (addressList.isEmpty()) {
                                        addressList = List.of("UNKNOWN_" + channelTypeKey);
                                    }

                                    var envelope = new OutboundRoutingEnvelope(
                                            userId,
                                            channelTypeKey,
                                            addressList, // Pass the consolidated list of multiple emails/tokens
                                            metaList,
                                            renderedContent.title(),
                                            renderedContent.body()
                                    );

                                    return MessageBuilder.withPayload(envelope)
                                            .setHeader("targetChannel", physicalChannelName)
                                            .setHeader("IntegrationMessageHeaderAccessor.CORRELATION_ID", correlationId)
                                            .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER", sequenceNumber)
                                            .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", sequenceSize)
                                            .build();
                                }));

                        messageMonos.add(channelPipelineMono);
                    }

                    // Merge all completed single-envelope channel monos back into a master list sequence wrapper
                    return Flux.merge(messageMonos).collectList();
                });
    }

    /**
     * Resolves all active multi-endpoint target pairs from isolated database configurations.
     * Eradicates the restrictive single-record lock (.next()) to support parallel broadcasting.
     */
    private Flux<TargetContactPair> resolveTargetEnvelopes(Long userId, String channelType) {
        switch (channelType.toUpperCase()) {
            case "PUSH":
                return pushContactRepository.findPushByUserId(userId)
                        .filter(PushContactRow::active) // Stream all matching active devices
                        .map(row -> new TargetContactPair(
                                row.pushToken(),
                                Map.of("deviceId", row.deviceId(), "deviceType", row.deviceType())
                        ));
            case "SMS":
                return smsContactRepository.findSmsByUserId(userId)
                        .filter(SmsContactRow::active)
                        .map(row -> new TargetContactPair(row.phoneNumber(), Map.of()));
            case "TELEGRAM":
                return telegramContactRepository.findTelegramByUserId(userId)
                        .filter(TelegramContactRow::active)
                        .map(row -> new TargetContactPair(row.telegramChatId(), Map.of("username", row.telegramUsername())));
            case "EMAIL":
                return emailContactRepository.findEmailByUserId(userId)
                        .filter(EmailContactRow::active)
                        .map(row -> new TargetContactPair(row.emailAddress(), Map.of()));
            default:
                return Flux.empty();
        }
    }

    /**
     * Internal utility immutable helper record class to bind structural routing meta arrays.
     */
    private record TargetContactPair(String destinationValue, Map<String, String> meta) {}
}