package com.soaesps.gateway.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Thin reactive facade over Redis for gateway-level caching:
 * token blacklisting on logout, short-lived auth decisions, rate limiting.
 */
@Service
public class ReactiveCacheService {

    private final ReactiveRedisTemplate<String, String> template;

    public ReactiveCacheService(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> template) {
        this.template = template;
    }

    public Mono<String> get(String key) {
        return template.opsForValue().get(key);
    }

    public Mono<Boolean> put(String key, String value, Duration ttl) {
        return template.opsForValue().set(key, value, ttl);
    }

    public Mono<Boolean> putForever(String key, String value) {
        return template.opsForValue().set(key, value);
    }

    public Mono<Boolean> evict(String key) {
        return template.delete(key).map(count -> count > 0);
    }

    public Mono<Boolean> exists(String key) {
        return template.hasKey(key);
    }
}