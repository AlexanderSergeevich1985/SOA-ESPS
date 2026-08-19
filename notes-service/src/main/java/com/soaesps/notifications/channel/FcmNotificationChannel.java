package com.soaesps.notifications.channel;

import com.google.firebase.messaging.*;
import com.soaesps.notifications.notifications.NotificationMessage;
import com.soaesps.notifications.notifications.NotificationRecipient;
import com.soaesps.notifications.service.push.DeviceTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Push notification channel via Firebase Cloud Messaging (free, unlimited).
 * Tries after Telegram (priority 10) and before Email (priority 50).
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

    @Override
    public String id() {
        return "push";
    }

    /**
     * Checks if the system has any active push tokens for this user.
     * Updated to pull directly from the database to support multiple devices dynamically.
     */
    @Override
    public boolean supports(NotificationRecipient r) {
        if (r.userId() == null) {
            return false;
        }
        List<String> tokens = deviceTokenService.tokensFor(r.userId());
        return !tokens.isEmpty();
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public boolean send(NotificationMessage msg, NotificationRecipient r) {
        List<String> tokens = deviceTokenService.tokensFor(r.userId());
        if (tokens.isEmpty()) {
            log.warn("No active push tokens found for user {}", r.userId());
            return false;
        }

        // Safe data payload mapping to prevent NullPointerException
        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("userId", String.valueOf(r.userId()));
        if (msg.type() != null) {
            dataPayload.put("type", msg.type().name());
        }

        // Multicast message distributes a single payload to a collection of tokens efficiently
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                // Top-level notification automatically handles delivery to both Android and iOS
                .setNotification(Notification.builder()
                        .setTitle(msg.subject())
                        .setBody(msg.body())
                        .build())
                .putAllData(dataPayload)
                // Android-specific settings (channel ID mapping)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId("soa-esps-alerts")
                                .build())
                        .build())
                // iOS-specific settings (sound and system options, removing duplicate alert texts)
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.debug("FCM multicast status: {} success, {} failure",
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleBatchErrors(response, tokens, r.userId());
            }

            // Return true if at least one device received the notification successfully
            return response.getSuccessCount() > 0;
        } catch (FirebaseMessagingException e) {
            log.error("FCM critical multicast delivery failure for user {}", r.userId(), e);
            return false;
        }
    }

    /**
     * Inspects batch responses to identify individual stale device tokens that need invalidation.
     */
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
                        log.warn("FCM token is dead for user {} ({}), removing from database...", userId, code);
                        // Automated database synchronization: removes the stale token
                        deviceTokenService.unregister(deadToken);
                    } else {
                        log.error("FCM delivery failed for a single device token of user {} due to error: {}", userId, code);
                    }
                }
            }
        }
    }
}