package com.soaesps.notifications.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.BridgeFrom;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class IntegrationConfiguration {
    private String host = "localhost";

    private String DEFAULT_MESSAGE_QUEUE_NAME = "message_queue";

    private String DEFAULT_EXCHANGER_NAME = "message_queue";

    @Bean
    public MessageChannel amqpInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel pubSubFileChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @BridgeFrom(value = "pubSubFileChannel")
    public MessageChannel amqpOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(rabbitConnectionFactory());
    }

    @Bean
    public AsyncRabbitTemplate asyncRabbitTemplate(final ConnectionFactory rabbitConnectionFactory) {
        final AsyncRabbitTemplate asyncRabbitTemplate = new AsyncRabbitTemplate(rabbitConnectionFactory(),
                "sendMessageExchange",
                "sendMessageKey",
                "asyncReplyQueue");
        asyncRabbitTemplate.setReceiveTimeout(60000);
        asyncRabbitTemplate.setAutoStartup(true);

        return asyncRabbitTemplate;
    }

    @Bean
    public RabbitAdmin amqpAdmin() {
        return new RabbitAdmin(rabbitConnectionFactory());
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(final ConnectionFactory connectionFactory) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(DEFAULT_MESSAGE_QUEUE_NAME);
        container.setConcurrentConsumers(2);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return container;
    }

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setConnectionTimeout(3000);
        connectionFactory.setRequestedHeartBeat(30);

        return connectionFactory;
    }

    @Bean
    @InboundChannelAdapter(value = "pubSubFileChannel", poller = @Poller(fixedDelay = "1000"))
    public AmqpInboundChannelAdapter amqpInbound(final SimpleMessageListenerContainer listenerContainer,
                                                 final @Qualifier("amqpInputChannel") MessageChannel channel) {
        final AmqpInboundChannelAdapter source = new AmqpInboundChannelAdapter(listenerContainer);
        source.setMessageConverter(new Jackson2JsonMessageConverter());
        source.setOutputChannel(channel);

        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "amqpOutboundChannel")
    public AmqpOutboundEndpoint amqpOutbound() { //MessageHandler
        final AmqpOutboundEndpoint endpoint = new AmqpOutboundEndpoint(rabbitTemplate());
        endpoint.setExchangeName(DEFAULT_EXCHANGER_NAME);
        endpoint.setExpectReply(true);
        endpoint.setOutputChannel(new NullChannel()); //amqpReplyChannel()
        return endpoint;
    }
}