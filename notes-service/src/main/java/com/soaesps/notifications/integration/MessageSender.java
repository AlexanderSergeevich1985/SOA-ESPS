package com.soaesps.notifications.integration;

import com.soaesps.notifications.config.IntegrationConstant;
import com.soaesps.notifications.service.Impl.EmailSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import javax.mail.internet.MimeMessage;

@MessageEndpoint
public class MessageSender {
    @Autowired
    private EmailSender emailSender;

    @ServiceActivator(inputChannel = IntegrationConstant.MESSAGE_ACTIVATOR_CHANNEL)
    public void sendMessage(final MimeMessage message) {
        emailSender.send(message);
    }
}