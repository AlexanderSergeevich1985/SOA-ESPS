package com.soaesps.notifications.component;

import com.soaesps.notifications.channel.EmailNotificationChannel;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.soaesps.notifications.config.IntegrationConstant.AGGREGATOR_CHANNEL;
import static com.soaesps.notifications.config.IntegrationConstant.EMAIL_BRANCH_CHANNEL;

@Component
public class EmailBranchHandler {
    private static final Logger log = LoggerFactory.getLogger(EmailBranchHandler.class);

    private final EmailNotificationChannel emailNotificationChannel;

    public EmailBranchHandler(EmailNotificationChannel emailNotificationChannel) {
        this.emailNotificationChannel = emailNotificationChannel;
    }

    @ServiceActivator(inputChannel = EMAIL_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendEmail(Message<OutboundRoutingEnvelope> message) {
        OutboundRoutingEnvelope envelope = message.getPayload();
        Long userId = envelope.userId();

        log.debug("EmailBranchHandler processing EMAIL channel routing for user ID: {}", userId);

        // Guard clause: if the router discovered zero active email addresses in the database layer
        if (envelope.destinations().isEmpty() || envelope.destinations().contains("UNKNOWN_EMAIL")) {
            log.warn("Skipping EMAIL channel pipeline execution: No active email configurations found for user {}", userId);
            return MessageBuilder.withPayload(new BranchStatus(userId, "EMAIL", "SKIPPED_NO_CONTACT"))
                    .copyHeaders(message.getHeaders())
                    .build();
        }

        // Delegate the physical parallel send straight to the integrated emailSender bean dependency
        boolean isDelivered = emailNotificationChannel.send(envelope);

        String executionResult = isDelivered ? "SUCCESS" : "FAILED";
        BranchStatus branchStatus = new BranchStatus(userId, "EMAIL", executionResult);

        return MessageBuilder.withPayload(branchStatus)
                .copyHeaders(message.getHeaders())
                .build();
    }
}