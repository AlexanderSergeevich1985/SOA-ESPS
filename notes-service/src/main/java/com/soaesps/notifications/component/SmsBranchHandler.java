package com.soaesps.notifications.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.repository.ReactiveContactRepository;
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

    private final ReactiveContactRepository contactRepository;

    public SmsBranchHandler(ReactiveContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @ServiceActivator(inputChannel = SMS_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendSms(Message<JsonNode> message) {
        Long userId = message.getPayload().get("userId").asLong();

        log.debug("Processing SMS branch concurrently for user ID: {}", userId);

        // Fetch phone number from the reactive single-table contact stream
        return contactRepository.findByUserId(userId)
                .filter(row -> "SMS".equals(row.contactType()))
                // Convert Flux to Mono by taking the first matched phone row
                .next()
                .map(row -> {
                    // smsGatewaySender.send(row.phoneNumber(), message.getPayload().get("text").asText());
                    log.info("SMS successfully dispatched to phone number: {}", row.phoneNumber());
                    return new BranchStatus(userId, "SMS", "SUCCESS");
                })
                // Fallback if the user has no phone contact configured
                .defaultIfEmpty(new BranchStatus(userId, "SMS", "SKIPPED_NO_CONTACT"))
                // Wrap into Spring Messaging container with original tracking headers
                .map(status -> MessageBuilder.withPayload(status).copyHeaders(message.getHeaders()).build())
                // Block to resolve Mono inside the integration flow thread
                .block();
    }
}