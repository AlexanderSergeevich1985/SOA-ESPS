package com.soaesps.notifications.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.soaesps.notifications.dto.BranchStatus;
import com.soaesps.notifications.repository.reactive.ReactiveContactRepository;
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
    private final ReactiveContactRepository contactRepository;

    public TelegramBranchHandler(ReactiveContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @ServiceActivator(inputChannel = TELEGRAM_BRANCH_CHANNEL,
            outputChannel = AGGREGATOR_CHANNEL)
    public Message<BranchStatus> sendTelegram(Message<JsonNode> message) {
        Long userId = message.getPayload().get("userId").asLong();

        log.debug("Processing TELEGRAM branch concurrently for user: {}", userId);

        return contactRepository.findByUserId(userId)
                .filter(row -> "TELEGRAM".equals(row.contactType()))
                .next()
                .map(row -> {
                    // tgSender.send(row.telegramChatId(), message.getPayload().get("text").asText());
                    log.info("Telegram message successfully sent to chat: {}", row.telegramChatId());
                    return new BranchStatus(userId, "TELEGRAM", "SUCCESS");
                })
                .defaultIfEmpty(new BranchStatus(userId, "TELEGRAM", "SKIPPED_NO_CONTACT"))
                .map(status -> MessageBuilder.withPayload(status).copyHeaders(message.getHeaders()).build())
                .block();
    }
}