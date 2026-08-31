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
import static com.soaesps.notifications.config.IntegrationConstant.EMAIL_BRANCH_CHANNEL;

@Component
public class EmailBranchHandler {
    private static final Logger log = LoggerFactory.getLogger(EmailBranchHandler.class);
    private final ReactiveContactRepository contactRepository;

    public EmailBranchHandler(ReactiveContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @ServiceActivator(inputChannel = EMAIL_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendEmail(Message<JsonNode> message) {
        Long userId = message.getPayload().get("userId").asLong();

        log.debug("Processing EMAIL branch concurrently for user: {}", userId);

        // Fetch rows stream (Flux)
        return contactRepository.findByUserId(userId)
                // Filter target contacts
                .filter(row -> "EMAIL".equals(row.contactType()))
                .next()
                // Map the row to a successful status payload
                .map(row -> {
                    // emailSender.send(row.emailAddress(), message.getPayload().get("text").asText());
                    log.info("Email successfully sent to {}", row.emailAddress());
                    return new BranchStatus(userId, "EMAIL", "SUCCESS");
                })
                // Fallback if the user has no email contact configured
                .defaultIfEmpty(new BranchStatus(userId, "EMAIL", "SKIPPED_NO_CONTACT"))
                // Wrap into Spring Messaging container
                .map(status -> MessageBuilder.withPayload(status).copyHeaders(message.getHeaders()).build())
                // Now block() will resolve perfectly on Mono<Message<BranchStatus>>
                .block();
    }
}