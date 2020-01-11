package com.soaesps.aggregator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    private static String[] authorities = new String[] {
            "INNER_SERVER", "VIEW_STATISTIC"
    };

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/aggregator")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    protected void configureInbound(final MessageSecurityMetadataSourceRegistry message) {
        message.nullDestMatcher()
                .permitAll()
                .simpDestMatchers("/app/**")
                .hasAnyAuthority(authorities)
                .simpSubscribeDestMatchers("/topic/" + "**")
                .permitAll()
                .anyMessage()
                .denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}