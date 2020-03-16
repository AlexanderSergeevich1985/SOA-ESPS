package com.soaesps.notifications.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MessageBuilder {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(MessageBuilder.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private ObjectMapper mapper;

    public Message createMessage(final Object object, final Map<String, Object> headers) throws JsonProcessingException {
        final MessageProperties properties = getMessageProperties(headers);

        return new Message(mapper.writeValueAsBytes(object), properties);
    }

    public Message createMessage(final Map<String, Object> headers, final Object object) {
        final MessageProperties properties = getMessageProperties(headers);
        try {
            return new Message(mapper.writeValueAsBytes(object), properties);
        } catch (final JsonProcessingException ex) {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[MessageBuilder/createMessage]: haven't wrote message because of {}", ex);
            }

            return new Message(null, properties);
        }
    }

    static public MessageProperties getMessageProperties(final Map<String, Object> headers) {
        final MessageProperties properties = new MessageProperties();
        headers.entrySet().stream().forEach(entry -> properties.setHeader(entry.getKey(), entry.getValue()));

        return properties;
    }

    static public MessageProperties getMessageProperties(final String key, final String value) {
        final MessageProperties properties = new MessageProperties();
        properties.setHeader(key, value);

        return properties;
    }

}