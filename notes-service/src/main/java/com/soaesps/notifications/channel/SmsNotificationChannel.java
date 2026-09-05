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
 * High-performance non-blocking SMS notification channel powered by WebClient.
 * Concurrently dispatches flat text payloads to multiple phone destinations for a single user.
 */
@Component
public class SmsNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationChannel.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String senderId;
    private final boolean enabled;
    private final String smsApiUrl;

    public SmsNotificationChannel(
            WebClient.Builder webClientBuilder,
            @Value("${notification.sms.api-key:test_key}") String apiKey,
            @Value("${notification.sms.sender-id:INFO}") String senderId,
            @Value("${notification.sms.enabled:false}") boolean enabled,
            @Value("${notification.sms.api-url:https://smsprovider.com}") String smsApiUrl) {
        this.webClient = webClientBuilder.build();
        this.apiKey = apiKey;
        this.senderId = senderId;
        this.enabled = enabled;
        this.smsApiUrl = smsApiUrl;
    }

    @Override
    public boolean send(OutboundRoutingEnvelope envelope) {
        Long userId = envelope.userId();
        List<String> phoneNumbers = envelope.destinations();

        // SMS usually drops the custom subject title and delivers only a clean flat message body text
        String flatSmsText = envelope.messageBody();

        // Process all target phone endpoints concurrently using non-blocking WebClient streams
        List<Boolean> dispatchResults = Flux.fromIterable(phoneNumbers)
                .flatMap(phoneNumber -> executeSmsPostRequest(phoneNumber, flatSmsText, userId))
                .collectList()
                .block(); // Block safely within Spring Integration dedicated handler thread partition boundaries

        return dispatchResults != null && dispatchResults.contains(Boolean.TRUE);
    }

    /**
     * Executes an isolated non-blocking HTTP POST request to the remote SMS provider gateway API.
     */
    private Mono<Boolean> executeSmsPostRequest(String phoneNumber, String text, Long userId) {
        return webClient.post()
                .uri(smsApiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "sender", senderId,       // Alpha name displayed to the user (e.g., 'BANK')
                        "recipient", phoneNumber, // Dynamic targeted active phone number
                        "message", text
                ))
                .retrieve()
                .toBodilessEntity() // Instantly drop raw provider response bodies to optimize heap memory footprint
                .map(response -> {
                    log.info("SMS message notification successfully broadcasted via WebClient to: {}", phoneNumber);
                    return Boolean.TRUE;
                })
                .onErrorResume(ex -> {
                    log.error("SMS HTTP gateway connection fallback failed for phone: {} (userId: {})", phoneNumber, userId, ex);
                    return Mono.just(Boolean.FALSE);
                });
    }

    @Override
    public String id() {
        return "sms";
    }

    @Override
    public boolean supports(OutboundRoutingEnvelope envelope) {
        if (envelope == null || envelope.destinations().isEmpty()) {
            return false;
        }
        return enabled
                && !apiKey.isBlank()
                && !envelope.destinations().contains("UNKNOWN_SMS");
    }
}