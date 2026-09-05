package com.soaesps.notifications.channel;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import com.soaesps.notifications.service.push.DeviceTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
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
    private final DeviceTokenService deviceTokenService;

    /**
     * Dependency injection via constructor.
     * Injects the FCM client and the token service for database synchronization.
     */
    public FcmNotificationChannel(FirebaseMessaging firebaseMessaging, DeviceTokenService deviceTokenService) {
        this.firebaseMessaging = firebaseMessaging;
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * Modernized non-blocking dispatch method driven by the enriched OutboundRoutingEnvelope pipeline.
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

        // Execution step: Trigger the completely non-blocking network broadcast task
        BatchResponse response = Mono.defer(() -> {
                    // 1. Invoke the native non-blocking async method from Google SDK
                    ApiFuture<BatchResponse> apiFuture = firebaseMessaging.sendEachForMulticastAsync(message);

                    // 2. Wrap ApiFuture into standard Java CompletableFuture using custom adapter logic
                    CompletableFuture<BatchResponse> completableFuture = convertToCompletableFuture(apiFuture);

                    return Mono.fromFuture(completableFuture);
                })
                .doOnNext(batchResponse -> {
                    log.info("FCM async multicast complete for user {}: {} successes, {} failures",
                            userId, batchResponse.getSuccessCount(), batchResponse.getFailureCount());

                    if (batchResponse.getFailureCount() > 0) {
                        // Offload database sync unregister calls to elastic threads to avoid locking netty
                        Mono.fromRunnable(() -> handleBatchErrors(batchResponse, tokens, userId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
                    }
                })
                .onErrorResume(ex -> {
                    log.error("FCM async network pipe delivery failure for user {}", userId, ex);
                    return Mono.empty();
                })
                .block(); // Block safely within Spring Integration dedicated handler thread partition boundaries

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

    private void handleBatchErrors(BatchResponse response, List<String> tokens, Long userId) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse res = responses.get(i);
            if (!res.isSuccessful()) {
                FirebaseMessagingException ex = res.getException();
                if (ex != null) {
                    MessagingErrorCode code = ex.getMessagingErrorCode();
                    String deadToken = tokens.get(i);

                    if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                        log.warn("FCM token is dead for user {} ({}), evicting from database...", userId, code);
                        deviceTokenService.unregister(deadToken);
                    } else {
                        log.error("FCM delivery failed for a single device token index [{}] of user {} due to error: {}", i, userId, code);
                    }
                }
            }
        }
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