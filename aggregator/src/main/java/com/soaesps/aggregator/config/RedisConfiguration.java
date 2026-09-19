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

import java.util.List;
import java.util.Map;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
    @Value("${redis.master:master}")
    private String master;

    @Value("${redis.port:6379}")
    private Integer port;

    @Value("${redis.factory.type:Lettuce}")
    private String type;

    @Value("#{'${redis.addresses:}'.split(',')}")
    private List<String> addresses;

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

        addresses.stream()
                .filter(addr -> addr != null && !addr.isBlank())
                .forEach(node -> {
                    String[] parts = node.split(":");
                    String host = parts[0].trim();
                    int nodePort = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 26379;
                    sentinelConfig.sentinel(host, nodePort);
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