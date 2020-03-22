package com.soaesps.notifications.integration;

import com.soaesps.notifications.config.IntegrationConstant;
import com.soaesps.notifications.domain.Message;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(name = "messageGateway",
        defaultRequestChannel = IntegrationConstant.GATEWAY_CHANNEL)
public interface MessageGateway {
    @Gateway
    void processMessage(final Message message);
}