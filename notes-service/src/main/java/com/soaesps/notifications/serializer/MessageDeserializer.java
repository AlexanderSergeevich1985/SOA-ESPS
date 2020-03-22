package com.soaesps.notifications.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.soaesps.core.Utils.JsonUtil;
import com.soaesps.notifications.domain.Message;

import java.io.IOException;

public class MessageDeserializer<T> extends JsonDeserializer<T> {
    @Override
    public T deserialize(final JsonParser arg0, final DeserializationContext arg1) throws IOException {
        final Message message = JsonUtil.fromString(arg0.getText(), Message.class);

        return (T) JsonUtil.fromString(message.getBody(), message.getClazz());
    }
}