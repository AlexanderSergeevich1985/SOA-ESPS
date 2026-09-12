package com.soaesps.notifications.channel;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import com.soaesps.notifications.repository.reactive.ReactivePushContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Push notification channel via Firebase Cloud Messaging.
 * Fully supports multi-device delivery via FCM Multicast API.
 */
@Component
@ConditionalOnProperty(name = "notification.push.enabled", havingValue = "true")
public class FcmNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationChannel.class);

    private final FirebaseMessaging firebaseMessaging;
    private final ReactivePushContactRepository pushContactRepository;

    /**
     * Dependency injection via constructor.
     * Injects the FCM client and the token service for database synchronization.
     */
    public FcmNotificationChannel(FirebaseMessaging firebaseMessaging, ReactivePushContactRepository pushContactRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.pushContactRepository = pushContactRepository;
    }

    /**
     * Dispatch method strictly driven by the synchronous NotificationChannel interface contract.
     * Evaluates non-blocking streams and invokes .block() safely on an isolated elastic thread partition.
     *
     * @param envelope Integrated delivery metadata containing destinations array, title, and body strings
     * @return true if at least one target device token received the push event payload successfully
     */
    @Override
    public boolean send(OutboundRoutingEnvelope envelope) {
        Long userId = envelope.userId();
        List<String> tokens = envelope.destinations();

        if (tokens.isEmpty()) {
            log.warn("No active push tokens supplied inside envelope for user {}", userId);
            return false;
        }

        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("userId", String.valueOf(userId));
        dataPayload.put("channelType", envelope.channelType());

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(envelope.messageTitle())
                        .setBody(envelope.messageBody())
                        .build())
                .putAllData(dataPayload)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId("soa-esps-alerts")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build())
                .build();

        // Execution step: Trigger the pipeline and safely block inside isolated boundedElastic pool threads
        BatchResponse response = Mono.defer(() -> {
                    // Invoke the native non-blocking async method from Google SDK
                    ApiFuture<BatchResponse> apiFuture = firebaseMessaging.sendEachForMulticastAsync(message);

                    // Wrap ApiFuture into standard Java CompletableFuture using custom adapter logic
                    CompletableFuture<BatchResponse> completableFuture = convertToCompletableFuture(apiFuture);

                    return Mono.fromFuture(completableFuture);
                })
                .flatMap(batchResponse -> {
                    log.info("FCM async multicast complete for user {}: {} successes, {} failures",
                            userId, batchResponse.getSuccessCount(), batchResponse.getFailureCount());

                    if (batchResponse.getFailureCount() > 0) {
                        // Securely trigger dead tokens eviction pipeline and wait for database execution logs
                        return handleBatchErrorsReactive(batchResponse, tokens, userId)
                                .then(Mono.just(batchResponse));
                    }

                    return Mono.just(batchResponse);
                })
                .onErrorResume(ex -> {
                    log.error("FCM async network pipe delivery failure for user {}", userId, ex);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic()) // Protect Netty Event Loop by offloading blocking task
                .block(); // Block safely as forced by the 'boolean' interface return signature

        return response != null && response.getSuccessCount() > 0;
    }

    /**
     * Utility bridging adapter transforming legacy Google ApiFuture structures into standard CompletableFuture instances.
     */
    private static <T> CompletableFuture<T> convertToCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        apiFuture.addListener(() -> {
            try {
                completableFuture.complete(apiFuture.get());
            } catch (Exception ex) {
                completableFuture.completeExceptionally(ex);
            }
        }, Runnable::run);
        return completableFuture;
    }

    /**
     * Internal private validation engine processing batch errors.
     * Correctly configured to return Mono<Void> to allow proper reactive chaining pipelines.
     */
    private Mono<Void> handleBatchErrorsReactive(BatchResponse response, List<String> tokens, Long userId) {
        List<SendResponse> responses = response.getResponses();

        return Flux.range(0, responses.size())
                .flatMap(i -> {
                    SendResponse res = responses.get(i);
                    if (res.isSuccessful()) {
                        return Mono.empty();
                    }

                    FirebaseMessagingException ex = res.getException();
                    if (ex == null) {
                        return Mono.empty();
                    }

                    MessagingErrorCode code = ex.getMessagingErrorCode();
                    String deadToken = tokens.get(i);

                    if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                        log.warn("FCM token is dead for user {} ({}), evicting from database...", userId, code);

                        return pushContactRepository.deleteByPushToken(deadToken)
                                .doOnSuccess(v -> log.info("Successfully dropped dead push record for token context"))
                                .onErrorResume(err -> {
                                    log.error("Failed to unregister dead push token from database", err);
                                    return Mono.empty();
                                });
                    } else {
                        log.error("FCM delivery failed for a single device token index [{}] of user {} due to error: {}", i, userId, code);
                        return Mono.empty();
                    }
                })
                .then();
    }

    @Override
    public String id() {
        return "push";
    }

    @Override
    public boolean supports(OutboundRoutingEnvelope envelope) {
        if (envelope == null || envelope.userId() == null) {
            return false;
        }
        // Validates that the router actively found non-empty token fields for this execution branch
        return !envelope.destinations().isEmpty() && !envelope.destinations().contains("UNKNOWN_PUSH");
    }
}