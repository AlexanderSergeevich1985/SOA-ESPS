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
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

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
    public MessageChannel emailBranchChannel() { return MessageChannels.flux().getObject(); }

    @Bean(TELEGRAM_BRANCH_CHANNEL)
    public MessageChannel telegramBranchChannel() { return MessageChannels.flux().getObject(); }

    @Bean(SMS_BRANCH_CHANNEL)
    public MessageChannel smsBranchChannel() { return MessageChannels.flux().getObject(); }

    @Bean(PUSH_BRANCH_CHANNEL)
    public MessageChannel pushBranchChannel() { return MessageChannels.flux().getObject(); }

    /**
     * Reactive internal bridge channel to switch standard flows to Project Reactor streams.
     */
    @Bean(REACTIVE_ROUTER_BRIDGE)
    public MessageChannel reactiveRouterBridgeChannel() {
        return MessageChannels.flux().getObject();
    }

    /**
     * Step 1: Bridge standard channel to a reactive FluxMessageChannel
     */
    @Transformer(inputChannel = KAFKA_INPUT_CHANNEL, outputChannel = REACTIVE_ROUTER_BRIDGE)
    public Message<JsonNode> bridgeToReactiveRouter(Message<JsonNode> message) {
        return message;
    }

    @Bean
    public IntegrationFlow reactiveRouterFlow(ReactiveScatterGatherRouter router) {
        return IntegrationFlow.from(REACTIVE_ROUTER_BRIDGE)
                .split(Message.class, message -> {
                    InboundNotificationEvent event = (InboundNotificationEvent) message.getPayload();
                    return router.routeAllowedChannelsReactive(event, message.getHeaders());
                })
                .route(Message.class, message ->
                        message.getHeaders().get("targetChannel", String.class)
                )
                .get();
    }

    public Flux<Message<OutboundRoutingEnvelope>> routeAllowedChannelsReactive(InboundNotificationEvent event, Map<String, Object> headers) {
        UUID userId = event.userId();
        String correlationId = Objects.requireNonNull(headers.get("id")).toString();

        if (userId == null) {
            OutboundRoutingEnvelope envelope = new OutboundRoutingEnvelope(UUID.randomUUID(),
                    "EMAIL",                    // EMAIL, TELEGRAM, SMS, PUSH
                    List.of("zbrauberg@mail.ru"),             // Collection of target addresses (e.g., [email1, email2])
                    new ArrayList<>(), // Metadata array matching the destinations index size
                    "Hello",                   // Fully compiled Thymeleaf layout title/subject
                    "Hello");
            Message<OutboundRoutingEnvelope> msg = MessageBuilder.withPayload(envelope)
                    .setHeader("targetChannel", EMAIL_BRANCH_CHANNEL)
                    .setHeader("IntegrationMessageHeaderAccessor.CORRELATION_ID", "some-uuid-string")
                    .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER", 1)
                    .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", 1)
                    .build();
            return Flux.just(msg);
        }

        Mono<List<String>> disabledChannelsMono = (userId == null)
                ? Mono.just(new java.util.ArrayList<>())
                : disabledChannelsRepository.findChannelsByUserId(userId)
                .collectList()
                .defaultIfEmpty(new java.util.ArrayList<>());

        return disabledChannelsMono.flatMapMany(disabledChannels -> {
            List<String> targetChannels = new ArrayList<>();
            if (!disabledChannels.contains("EMAIL")) targetChannels.add(EMAIL_BRANCH_CHANNEL);
            if (!disabledChannels.contains("TELEGRAM")) targetChannels.add(TELEGRAM_BRANCH_CHANNEL);
            if (!disabledChannels.contains("SMS")) targetChannels.add(SMS_BRANCH_CHANNEL);
            if (!disabledChannels.contains("PUSH")) targetChannels.add(PUSH_BRANCH_CHANNEL);

            int sequenceSize = targetChannels.size();

            if (sequenceSize == 0) {
                log.warn("All messaging channels are muted for userId: {}.", userId);
                var mutedEnvelope = new OutboundRoutingEnvelope(userId, "NONE", List.of("NONE"), List.of(Map.of()), "MUTED", "MUTED");
                return Flux.just(MessageBuilder.withPayload(mutedEnvelope)
                        .setHeader("targetChannel", "NONE")
                        .setHeader("IntegrationMessageHeaderAccessor.CORRELATION_ID", correlationId)
                        .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", 0)
                        .build());
            }

            List<Mono<Message<OutboundRoutingEnvelope>>> messageMonos = new ArrayList<>();

            for (int i = 0; i < sequenceSize; i++) {
                String physicalChannelName = targetChannels.get(i);
                String channelTypeKey = physicalChannelName.replace("BranchChannel", "").toUpperCase();
                int sequenceNumber = i + 1;

                Mono<ReactiveHtmlRenderService.RenderedContent> contentRenderMono = templateEngine.renderAsync(channelTypeKey, event).cache();

                Mono<Message<OutboundRoutingEnvelope>> channelPipelineMono = resolveTargetEnvelopes(userId, channelTypeKey)
                        .collectList()
                        .flatMap(targetPairs -> contentRenderMono.map(renderedContent -> {
                            List<String> addressList = targetPairs.stream().map(TargetContactPair::destinationValue).toList();
                            List<Map<String, String>> metaList = targetPairs.stream().map(TargetContactPair::meta).toList();

                            if (addressList.isEmpty()) {
                                addressList = List.of("UNKNOWN_" + channelTypeKey);
                            }

                            var envelope = new OutboundRoutingEnvelope(
                                    userId,
                                    channelTypeKey,
                                    addressList,
                                    metaList,
                                    renderedContent.title(),
                                    renderedContent.body()
                            );

                            return MessageBuilder.withPayload(envelope)
                                    .setHeader("targetChannel", physicalChannelName)
                                    .copyHeaders(headers)
                                    .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER", sequenceNumber)
                                    .setHeader("IntegrationMessageHeaderAccessor.SEQUENCE_SIZE", sequenceSize)
                                    .build();
                        }));

                messageMonos.add(channelPipelineMono);
            }

            return Flux.merge(messageMonos);
        });
    }


    /**
     * Step 2: Asynchronously fetches blocklists from the R2DBC repository
     * and evaluates active channel routing inside a non-blocking stream context.
     */

    /**
     * Resolves all active multi-endpoint target pairs from isolated database configurations.
     * Eradicates the restrictive single-record lock (.next()) to support parallel broadcasting.
     */
    private Flux<TargetContactPair> resolveTargetEnvelopes(UUID userId, String channelType) {
        return switch (channelType.toUpperCase()) {
            case "PUSH" -> pushContactRepository.findPushByUserId(userId)
                    .filter(PushContactRow::active) // Stream all matching active devices
                    .map(row -> new TargetContactPair(
                            row.pushToken(),
                            Map.of("deviceId", row.deviceId(), "deviceType", row.deviceType())
                    ));
            case "SMS" -> smsContactRepository.findSmsByUserId(userId)
                    .filter(SmsContactRow::active)
                    .map(row -> new TargetContactPair(row.phoneNumber(), Map.of()));
            case "TELEGRAM" -> telegramContactRepository.findTelegramByUserId(userId)
                    .filter(TelegramContactRow::active)
                    .map(row -> new TargetContactPair(row.telegramChatId(), Map.of("username", row.telegramUsername())));
            case "EMAIL" -> emailContactRepository.findEmailByUserId(userId)
                    .filter(EmailContactRow::active)
                    .map(row -> new TargetContactPair(row.emailAddress(), Map.of()));
            default -> Flux.empty();
        };
    }

    /**
     * Internal utility immutable helper record class to bind structural routing meta arrays.
     */
    private record TargetContactPair(String destinationValue, Map<String, String> meta) {}
}