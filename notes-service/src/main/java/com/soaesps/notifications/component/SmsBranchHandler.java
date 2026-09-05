package com.soaesps.notifications.component;

import com.soaesps.notifications.channel.SmsNotificationChannel;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.soaesps.notifications.config.IntegrationConstant.AGGREGATOR_CHANNEL;
import static com.soaesps.notifications.config.IntegrationConstant.SMS_BRANCH_CHANNEL;

/**
 * Concurrent execution branch handler for SMS notifications.
 * Extracts user's phone number from the single-table hierarchy,
 * dispatches payloads via SMS gateway, and notifies the centralized aggregator.
 */
@Component
public class SmsBranchHandler {
    private static final Logger log = LoggerFactory.getLogger(SmsBranchHandler.class);

    private final SmsNotificationChannel smsNotificationChannel;

    public SmsBranchHandler(SmsNotificationChannel smsNotificationChannel) {
        this.smsNotificationChannel = smsNotificationChannel;
    }

    @ServiceActivator(inputChannel = SMS_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendSms(Message<OutboundRoutingEnvelope> message) {
        OutboundRoutingEnvelope envelope = message.getPayload();
        Long userId = envelope.userId();

        log.debug("SmsBranchHandler processing SMS channel routing for user ID: {}", userId);

        // Guard clause: if the router discovered zero active phone numbers inside the database layer
        if (envelope.destinations().isEmpty() || envelope.destinations().contains("UNKNOWN_SMS")) {
            log.warn("Skipping SMS channel pipeline execution: No active phone numbers found for user {}", userId);
            return MessageBuilder.withPayload(new BranchStatus(userId, "SMS", "SKIPPED_NO_CONTACT"))
                    .copyHeaders(message.getHeaders())
                    .build();
        }

        // Delegate the physical parallel send straight to the integrated channel bean dependency
        boolean isDelivered = smsNotificationChannel.send(envelope);

        String executionResult = isDelivered ? "SUCCESS" : "FAILED";
        BranchStatus branchStatus = new BranchStatus(userId, "SMS", executionResult);

        return MessageBuilder.withPayload(branchStatus)
                .copyHeaders(message.getHeaders())
                .build();
    }
}