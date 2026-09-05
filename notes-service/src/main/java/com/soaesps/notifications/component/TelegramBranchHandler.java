package com.soaesps.notifications.component;

import com.soaesps.notifications.channel.TelegramNotificationChannel;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.soaesps.notifications.config.IntegrationConstant.AGGREGATOR_CHANNEL;
import static com.soaesps.notifications.config.IntegrationConstant.TELEGRAM_BRANCH_CHANNEL;

@Component
public class TelegramBranchHandler {
    private static final Logger log = LoggerFactory.getLogger(TelegramBranchHandler.class);

    private final TelegramNotificationChannel telegramNotificationChannel;

    public TelegramBranchHandler(TelegramNotificationChannel telegramNotificationChannel) {
        this.telegramNotificationChannel = telegramNotificationChannel;
    }

    @ServiceActivator(inputChannel = TELEGRAM_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendTelegram(Message<OutboundRoutingEnvelope> message) {
        OutboundRoutingEnvelope envelope = message.getPayload();
        Long userId = envelope.userId();

        log.debug("TelegramBranchHandler processing TELEGRAM channel routing for user ID: {}", userId);

        // Guard clause: if the router discovered zero active telegram chat IDs in the database layer
        if (envelope.destinations().isEmpty() || envelope.destinations().contains("UNKNOWN_TELEGRAM")) {
            log.warn("Skipping TELEGRAM channel pipeline execution: No active telegram configurations found for user {}", userId);
            return MessageBuilder.withPayload(new BranchStatus(userId, "TELEGRAM", "SKIPPED_NO_CONTACT"))
                    .copyHeaders(message.getHeaders())
                    .build();
        }

        // Delegate the physical parallel send straight to the integrated telegramNotificationChannel bean dependency
        boolean isDelivered = telegramNotificationChannel.send(envelope);

        String executionResult = isDelivered ? "SUCCESS" : "FAILED";
        BranchStatus branchStatus = new BranchStatus(userId, "TELEGRAM", executionResult);

        return MessageBuilder.withPayload(branchStatus)
                .copyHeaders(message.getHeaders())
                .build();
    }
}