package com.soaesps.coordinator.filter;

import io.github.bucket4j.*;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Bucket per client (keyed by IP or API key)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Configuration: 100 requests per minute per client
    private static final int MAX_REQUESTS = 100;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientKey = getClientKey(request);
        Bucket bucket = getOrCreateBucket(clientKey);

        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded. Please retry after %d seconds",
                    "retryAfter": %d
                }
                """.formatted(
                    probe.getNanosToWaitForRefill() / 1_000_000_000,
                    probe.getNanosToWaitForRefill() / 1_000_000_000
            ));

            log.warn("Rate limit exceeded for client: {}", clientKey);
        }
    }

    private String getClientKey(HttpServletRequest request) {
        // Use API key if present, otherwise use IP
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api-key:" + apiKey;
        }

        // Fallback to IP address
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }

    private Bucket getOrCreateBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_REQUESTS)
                .refillIntervally(MAX_REQUESTS, REFILL_DURATION)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // Optional: cleanup old buckets to prevent memory leak
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupOldBuckets() {
        // Implement LRU or TTL-based cleanup if needed
        if (buckets.size() > 10_000) {
            log.warn("Bucket map size: {}, consider implementing TTL cleanup", buckets.size());
        }
    }
}