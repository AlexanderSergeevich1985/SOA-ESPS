package com.soaesps.notifications.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableRabbit
public class RabbitMQConfiguration implements RabbitListenerConfigurer {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(RabbitMQConfiguration.class.getName());
        logger.setLevel(Level.INFO);
    }


    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private String port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.listener.direct.consumers-per-queue}")
    private Integer consumersPerQueue;

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(Integer.valueOf(port));
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        logger.log(Level.INFO, "[RabbitMQConfiguration/cachingConnectionFactory]: have been configured");

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final CachingConnectionFactory cachingConnectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(cachingConnectionFactory);
        template.setReplyTimeout(1000000);
        logger.log(Level.INFO, "[RabbitMQConfiguration/rabbitTemplate]: have been configured");

        return template;
    }

    @Bean(name = "mainNotificationFactory")
    public SimpleRabbitListenerContainerFactory mainNotificationContainerFactory(final CachingConnectionFactory cachingConnectionFactory) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        factory.setConcurrentConsumers(consumersPerQueue);
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(1);
        logger.log(Level.INFO, "[RabbitMQConfiguration/mainNotificationContainerFactory]: have been configured");

        return factory;
    }

    @Bean
    public DefaultMessageHandlerMethodFactory defaultMessageHandlerMethodFactory() {
        final DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(new MappingJackson2MessageConverter());

        return factory;
    }

    @Override
    public void configureRabbitListeners(final RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(defaultMessageHandlerMethodFactory());
    }
}