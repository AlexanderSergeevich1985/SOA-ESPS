package com.soaesps.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Non-blocking cache configuration for the reactive API gateway.
 *
 * <p>The {@link ReactiveRedisConnectionFactory} (Lettuce) is auto-configured by
 * Spring Boot from {@code spring.data.redis.*} properties — no manual factory bean needed.
 *
 * <p>IMPORTANT: do NOT use blocking {@code RedisTemplate} or {@code @Cacheable} here —
 * they would block the Netty event loop and degrade gateway throughput.
 */
@Configuration
public class CacheConfiguration {

    /**
     * Plain string-to-string template: JWT blacklists, rate-limit counters,
     * simple flags and tokens.
     */
    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }

    /**
     * JSON template for caching structured payloads (e.g. resolved user authorities,
     * service metadata) without manual serialization.
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> jsonRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer json = new GenericJackson2JsonRedisSerializer();

        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .value(json)
                .hashKey(new StringRedisSerializer())
                .hashValue(json)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}