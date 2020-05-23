package com.soaesps.profile.config;

import com.soaesps.core.integration.IntegrationConstant;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@EnableRabbit
@Configuration
public class RabbitMQConfig implements RabbitListenerConfigurer {
    private String queueName = IntegrationConstant
            .Exchanges
            .USER_PROFILE_OUEUE
            .getExchangeName();

    private String topicExchangeName = IntegrationConstant
            .Topics
            .USER_PROFILE_OUEUE
            .getTopicName();

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.template.reply-timeout}")
    private Duration timeout;

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate template = new RabbitTemplate(cachingConnectionFactory());
        template.setReplyTimeout(timeout.toMillis());

        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory configureListenerContainerFactory() {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setDefaultRequeueRejected(false);
        factory.setConnectionFactory(cachingConnectionFactory());

        return factory;
    }

    @Override
    public void configureRabbitListeners(final RabbitListenerEndpointRegistrar
                                                     rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setContainerFactory(configureListenerContainerFactory());
    }
}