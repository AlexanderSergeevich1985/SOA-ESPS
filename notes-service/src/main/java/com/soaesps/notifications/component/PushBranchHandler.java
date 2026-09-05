package com.soaesps.notifications.component;

import com.soaesps.notifications.channel.FcmNotificationChannel;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.soaesps.notifications.config.IntegrationConstant.AGGREGATOR_CHANNEL;
import static com.soaesps.notifications.config.IntegrationConstant.PUSH_BRANCH_CHANNEL;

/**
 * Concurrent execution branch handler for PUSH notifications.
 * Extracts all active device tokens from the single-table hierarchy,
 * dispatches payloads, and notifies the centralized aggregator.
 */
@Component
public class PushBranchHandler {
    private static final Logger log = LoggerFactory.getLogger(PushBranchHandler.class);

    private final FcmNotificationChannel fcmNotificationChannel;

    public PushBranchHandler(FcmNotificationChannel fcmNotificationChannel) {
        this.fcmNotificationChannel = fcmNotificationChannel;
    }

    @ServiceActivator(inputChannel = PUSH_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendPushNotification(Message<OutboundRoutingEnvelope> message) {
        OutboundRoutingEnvelope envelope = message.getPayload();
        Long userId = envelope.userId();

        log.debug("PushBranchHandler processing PUSH channel routing for user ID: {}", userId);

        // Guard clause: if the router discovered zero active device tokens in the database layer
        if (envelope.destinations().isEmpty() || envelope.destinations().contains("UNKNOWN_PUSH")) {
            log.warn("Skipping PUSH channel pipeline execution: No active device tokens found for user {}", userId);
            return MessageBuilder.withPayload(new BranchStatus(userId, "PUSH", "SKIPPED_NO_CONTACT"))
                    .copyHeaders(message.getHeaders())
                    .build();
        }

        // Delegate the physical multicast send straight to the integrated channel bean dependency
        boolean isDelivered = fcmNotificationChannel.send(envelope);

        String executionResult = isDelivered ? "SUCCESS" : "FAILED";
        BranchStatus branchStatus = new BranchStatus(userId, "PUSH", executionResult);

        return MessageBuilder.withPayload(branchStatus)
                .copyHeaders(message.getHeaders())
                .build();
    }
}