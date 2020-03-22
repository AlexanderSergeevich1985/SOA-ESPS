package com.soaesps.notifications.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.notifications.config.IntegrationConstant;
import com.soaesps.notifications.domain.Message;
import com.soaesps.notifications.domain.SimpleMessage;
import com.soaesps.notifications.service.Impl.EmailSender;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;

import java.util.logging.Level;
import java.util.logging.Logger;

@MessageEndpoint
public class MessageTransformer {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(EmailSender.class.getName());
        logger.setLevel(Level.INFO);
    }

    private ObjectMapper mapper;

    @Transformer(inputChannel = IntegrationConstant.FILTER_CHANNEL,
            outputChannel = IntegrationConstant.TRANSFORMER_CHANNEL)
    public SimpleMessage transformMessage(final Message message) {
        try {
            return (SimpleMessage) mapper.readValue(message.getBody(), message.getClazz());
        } catch (JsonProcessingException ex) {
            logger.log(Level.INFO, "[MessageTransformer/transformMessage]: error {}", ex);

            return null;
        }
    }
}