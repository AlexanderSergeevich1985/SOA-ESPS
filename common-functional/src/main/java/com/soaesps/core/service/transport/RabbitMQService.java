package com.soaesps.core.service.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.soaesps.core.Utils.JsonUtil;
import com.soaesps.core.integration.IntegrationConstant;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMQService {
    private final static Logger logger;

    static {
        logger = Logger.getLogger(RabbitMQService.class.getName());
        logger.setLevel(Level.INFO);
    }

    private RabbitTemplate template;

    private ObjectMapper mapper;

    public RabbitMQService(RabbitTemplate template, ObjectMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    private <T> Collection<T> sendReceiveMessages(IntegrationConstant.Exchanges exchange, Object message,
                                             MessageProperties properties, Class<T> tClass) throws IOException {
        return sendReceiveMessages(exchange.getExchangeName(), exchange.getRouteKey(), message, properties, tClass);
    }

    private <T> T sendReceiveMsg(IntegrationConstant.Exchanges exchange, Object message,
                                 MessageProperties properties, Class<T> tClass) throws IOException {
        return sendReceiveMsg(exchange.getExchangeName(), exchange.getRouteKey(), message, properties, tClass);
    }

    private <T> Collection<T> sendReceiveMessages(String exchange, String routeKey, Object message,
                                                  MessageProperties properties, Class<T> tClass) throws IOException {
        Message response = template.sendAndReceive(exchange, routeKey, objToMsg(message, properties, mapper));
        if (response == null || response.getBody() == null
                || response.getBody().length == 0) {
            return null;
        }
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, tClass);

        return msgToObjList(response, mapper, type);
    }

    private <T> T sendReceiveMsg(String exchange, String routeKey, Object message, MessageProperties properties,
                                 Class<T> tClass) throws IOException {
        Message response = template.sendAndReceive(exchange, routeKey, objToMsg(message, properties, mapper));
        if (response == null || response.getBody() == null
                || response.getBody().length == 0) {
            return null;
        }

        return msgToObj(response, mapper, tClass);
    }

    private Message sendReceiveMsg(String exchange, String routeKey, Object message) throws IOException {
        return template.sendAndReceive(exchange, routeKey, objToMsg(message, mapper));
    }

    private Message sendReceiveMsg(String exchange, String routeKey, Object message,
                                   MessageProperties properties) throws IOException {
        return template.sendAndReceive(exchange, routeKey, objToMsg(message, properties, mapper));
    }

    public void sendMsg(String exchange, Object message) throws Exception {
        template.convertAndSend(exchange, objToMsg(message, mapper));
    }

    public void sendMsg(String exchange, Object message, MessageProperties properties) throws Exception {
        template.convertAndSend(exchange, objToMsg(message, properties, mapper));
    }

    public static Message objToMsg(Object message, ObjectMapper mapper) throws JsonProcessingException {
        return objToMsg(message, new MessageProperties(), mapper);
    }

    public static Message objToMsg(Object message, MessageProperties properties,
                                   ObjectMapper mapper) throws JsonProcessingException {
        return new Message(mapper.writeValueAsBytes(message), properties != null ? properties
                : new MessageProperties());
    }

    public static <T> T msgToObj(Message message, ObjectMapper mapper, Class<T> tClass) throws IOException {
        return mapper.readValue(JsonUtil.byteToString(message.getBody()), tClass);
    }

    public static <T> List<T> msgToObjList(Message message, ObjectMapper mapper,
                                           CollectionType type) throws IOException {
        return mapper.readValue(JsonUtil.byteToString(message.getBody()), type);
    }
}