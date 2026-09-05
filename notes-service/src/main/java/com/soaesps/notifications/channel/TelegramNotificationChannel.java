package com.soaesps.notifications.channel;

import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * High-performance non-blocking Telegram notification channel powered by WebClient.
 * Executes parallel multi-chat delivery streams for high throughput requirements.
 */
@Component
public class TelegramNotificationChannel implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationChannel.class);
    private static final int MAX_TG_LENGTH = 4096;

    private final WebClient webClient;
    private final String botToken;
    private final boolean enabled;
    private final String apiUrl;

    public TelegramNotificationChannel(
            WebClient.Builder webClientBuilder, // Best practice: inject Builder to customize timeouts later
            @Value("${notification.telegram.bot-token:}") String botToken,
            @Value("${notification.telegram.enabled:false}") boolean enabled) {
        this.webClient = webClientBuilder.build();
        this.botToken = botToken;
        this.enabled = enabled;
        this.apiUrl = String.format("https://telegram.org", botToken);

        if (enabled && botToken.isBlank()) {
            log.warn("Telegram notification channel is enabled, but bot-token is empty!");
        }
    }

    @Override
    public boolean send(OutboundRoutingEnvelope envelope) {
        Long userId = envelope.userId();
        List<String> chatIds = envelope.destinations();

        String text = "<b>%s</b>\n\n%s".formatted(
                escapeHtml(envelope.messageTitle()),
                escapeHtml(envelope.messageBody())
        );

        if (text.length() > MAX_TG_LENGTH) {
            text = text.substring(0, MAX_TG_LENGTH - 3) + "...";
        }

        // Final text payload variable for lambda scope constraints
        final String finalText = text;

        // Process all target chats concurrently using non-blocking Flux stream loops
        List<Boolean> results = Flux.fromIterable(chatIds)
                .flatMap(chatId -> executePostRequest(chatId, finalText, userId))
                .collectList()
                .block(); // Block safely inside Spring Integration dedicated thread partition boundary

        return results != null && results.contains(Boolean.TRUE);
    }

    /**
     * Executes an isolated non-blocking HTTP POST request to Telegram Bot API.
     */
    private Mono<Boolean> executePostRequest(String chatId, String text, Long userId) {
        return webClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "chat_id", chatId,
                        "text", text,
                        "parse_mode", "HTML"
                ))
                .retrieve()
                .toBodilessEntity() // Discard large string bodies to save memory footprint states
                .map(response -> {
                    log.info("Telegram notification successfully sent via WebClient to chatId: {}", chatId);
                    return Boolean.TRUE;
                })
                .onErrorResume(ex -> {
                    log.error("Telegram async HTTP delivery failed for chatId: {} (userId: {})", chatId, userId, ex);
                    return Mono.just(Boolean.FALSE);
                });
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    @Override
    public String id() {
        return "telegram";
    }

    @Override
    public boolean supports(OutboundRoutingEnvelope envelope) {
        if (envelope == null || envelope.destinations().isEmpty()) {
            return false;
        }
        return enabled
                && !botToken.isBlank()
                && !envelope.destinations().contains("UNKNOWN_TELEGRAM");
    }
}