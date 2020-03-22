package com.soaesps.notifications.integration;

import com.soaesps.notifications.config.IntegrationConstant;
import com.soaesps.notifications.domain.MessageType;
import com.soaesps.notifications.domain.SimpleMessage;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;

@MessageEndpoint
public class MessageRouter {
    @Router(inputChannel = IntegrationConstant.TRANSFORMER_CHANNEL)
    public String routeMessage(final SimpleMessage message) {
        if(MessageType.SIMPLE_MESSAGE.equals(message.getType())) {
            return IntegrationConstant.SIMPLE_ROUTER_CHANNEL;
        } else {
            return IntegrationConstant.AGG_ROUTER_CHANNEL;
        }
    }
}