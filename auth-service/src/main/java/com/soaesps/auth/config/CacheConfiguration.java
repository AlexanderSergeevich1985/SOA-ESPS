package com.soaesps.auth.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching(mode = AdviceMode.ASPECTJ)
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 1. Базовая конфигурация по умолчанию для всех кэшей
        RedisCacheConfiguration defaultConfig = buildRedisCacheConfiguration(Duration.ofMinutes(30));

        // 2. Индивидуальные настройки для конкретных регионов кэша (замена конфигурации из ehcache.xml)
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();

        // Регион STATIC_DATA (который был в вашем BaseUserDetails)
        redisCacheConfigurationMap.put("STATIC_DATA", buildRedisCacheConfiguration(Duration.ofHours(1)));

        // Пример: кэш для сессий или токенов
        redisCacheConfigurationMap.put("TOKENS", buildRedisCacheConfiguration(Duration.ofMinutes(15)));

        // 3. Сборка CacheManager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(redisCacheConfigurationMap)
                .transactionAware() // Аналог cacheManager.setTransactionAware(true) из EhCache
                .build();
    }

    /**
     * Вспомогательный метод для создания конфигурации кэша с кастомным TTL и сериализацией.
     */
    private RedisCacheConfiguration buildRedisCacheConfiguration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl) // Время жизни записей
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // Использование JSON-сериализатора делает данные читаемыми в консоли Redis (redis-cli)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // Не кэшировать null значения, чтобы избежать проблем с десериализацией
    }
}