package com.soaesps.aggregator.config;

import io.lettuce.core.ClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.Map;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
    @Value("redis.master")
    private String master;

    @Value("redis.port")
    private Integer port;

    @Value("redis.factory.type")
    private String type;

    @Value("#{'${redis.address}'.split(',')}")
    private Map<String, Integer> addresses;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        switch (type) {
            case "Jedis":
                return jedisConnectionFactory();
            case "Lettuce":
                return lettuceConnectionFactory();
            default:
                return jedisConnectionFactory();
        }
    }

    protected RedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(sentinelConfiguration());
    }

    protected RedisConnectionFactory lettuceConnectionFactory() {
        final RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(master, port);
        final LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions()).build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    protected RedisSentinelConfiguration sentinelConfiguration() {
        final RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master(master);
        addresses.entrySet().stream().forEach(i -> {
            sentinelConfig.sentinel(i.getKey(), i.getValue());
        });

        return sentinelConfig;
    }

    protected ClientOptions clientOptions() {
        final ClientOptions options = ClientOptions.builder()
                .autoReconnect(true).build();

        return options;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }
}