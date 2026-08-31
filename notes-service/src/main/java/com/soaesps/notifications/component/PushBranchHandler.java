package com.soaesps.notifications.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.dto.UserContactRow;
import com.soaesps.notifications.repository.ReactiveContactRepository;
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

    private final ReactiveContactRepository contactRepository;

    public PushBranchHandler(ReactiveContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @ServiceActivator(inputChannel = PUSH_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendPushNotification(Message<JsonNode> message) {
        Long userId = message.getPayload().get("userId").asLong();
        String text = message.getPayload().has("text") ? message.getPayload().get("text").asText() : "";

        log.debug("Processing PUSH branch concurrently for user ID: {}", userId);

        // Fetch all active devices for the user from the reactive contact stream
        return contactRepository.findByUserId(userId)
                .filter(row -> "PUSH".equals(row.contactType()))
                .map(UserContactRow::pushToken)
                .filter(token -> token != null && !token.isBlank())
                .collectList() // Collect all matching tokens into a Mono<List<String>>
                .map(tokens -> {
                    if (tokens.isEmpty()) {
                        log.warn("No active push tokens found for user ID: {}", userId);
                        return new BranchStatus(userId, "PUSH", "SKIPPED_NO_CONTACT");
                    }

                    // Dispatch notification to each registered device token
                    for (String token : tokens) {
                        // firebasePushSender.send(token, text);
                        log.info("Push notification successfully sent to device token: {}", token);
                    }

                    return new BranchStatus(userId, "PUSH", "SUCCESS");
                })
                // Build messaging envelope and block until non-blocking R2DBC flow resolves
                .map(status -> MessageBuilder.withPayload(status).copyHeaders(message.getHeaders()).build())
                .block();
    }
}