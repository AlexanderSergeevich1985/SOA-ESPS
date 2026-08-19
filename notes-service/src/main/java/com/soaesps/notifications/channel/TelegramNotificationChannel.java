package com.soaesps.notifications.channel;

import com.soaesps.notifications.notifications.NotificationMessage;
import com.soaesps.notifications.notifications.NotificationRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Component
public class TelegramNotificationChannel implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationChannel.class);
    private static final int MAX_TG_LENGTH = 4096;

    private final RestTemplate restTemplate;
    private final String botToken;
    private final boolean enabled;
    private final String apiUrl;

    public TelegramNotificationChannel(
            RestTemplate restTemplate,
            @Value("${notification.telegram.bot-token:}") String botToken,
            @Value("${notification.telegram.enabled:false}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.botToken = botToken;
        this.enabled = enabled;
        this.apiUrl = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        if (enabled && botToken.isBlank()) {
            log.warn("Telegram notification channel is enabled, but bot-token is empty!");
        }
    }

    @Override
    public String id() {
        return "telegram";
    }

    @Override
    public boolean supports(NotificationRecipient r) {
        return enabled
                && !botToken.isBlank()
                && r.telegramChatId() != null
                && !r.telegramChatId().isBlank();
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean send(NotificationMessage msg, NotificationRecipient r) {
        try {
            String text = "<b>%s</b>\n\n%s".formatted(
                    escapeHtml(msg.subject()),
                    escapeHtml(msg.body())
            );

            if (text.length() > MAX_TG_LENGTH) {
                text = text.substring(0, MAX_TG_LENGTH - 3) + "...";
            }

            restTemplate.postForEntity(
                    URI.create(apiUrl),
                    Map.of(
                            "chat_id", r.telegramChatId(),
                            "text", text,
                            "parse_mode", "HTML"
                    ),
                    String.class
            );
            return true;
        } catch (Exception e) {
            log.error("Telegram delivery failed for user {}", r.userId(), e);
            return false;
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}