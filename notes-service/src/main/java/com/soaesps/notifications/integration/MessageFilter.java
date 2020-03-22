package com.soaesps.notifications.integration;

import com.soaesps.notifications.domain.Message;
import com.soaesps.notifications.config.IntegrationConstant;

import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessageEndpoint;

@MessageEndpoint
public class MessageFilter {
    @Filter(inputChannel = IntegrationConstant.GATEWAY_CHANNEL, outputChannel = IntegrationConstant.FILTER_CHANNEL,
            discardChannel = IntegrationConstant.DISCARD_FILTER_CHANNEL)
    public boolean filterMessage(final Message message) {
        return message != null && message.getType() != null
                && message.getBody() != null && !message.getBody().isEmpty();
    }
}