package com.soaesps.core.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.soaesps.core.Utils.JsonUtil;
import org.springframework.amqp.core.Message;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.io.IOException;
import java.io.Serializable;

public class MsgEntityConverter implements ConverterFactory<Message, Serializable> {
    private ObjectMapper mapper;

    public MsgEntityConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T extends Serializable> Converter<Message, T> getConverter(Class<T> tClass) {
        return new MsgToEntityConverter<>(tClass, mapper);
    }

    private static class MsgToEntityConverter<T extends Serializable> implements Converter<Message, T> {
        ObjectMapper mapper;

        private Class<T> tClass;

        public MsgToEntityConverter(Class<T> tClass, ObjectMapper mapper) {
            this.tClass = tClass;
            this.mapper = mapper;
        }

        @Override
        public T convert(Message message) {
            String json = JsonUtil.byteToString(message.getBody());
            if (json == null) {
                return null;
            }
            T value;
            try {
                value = mapper.readValue(json, tClass);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }

            return value;
        }
    }
}