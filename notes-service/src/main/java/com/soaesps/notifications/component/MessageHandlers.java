package com.soaesps.notifications.component;

import com.soaesps.notifications.domain.MessageType;

import java.util.EnumMap;

public class MessageHandlers<T, T1> {
    private EnumMap<MessageType, MessageHandler<T, T1>> handlers;

    public MessageHandlers() {
        this.handlers = new EnumMap<>(MessageType.class);
    }

    public MessageHandler<T, T1> getMessageHandler(final MessageType type) {
        MessageHandler<T, T1> handler = handlers.get(type);
        if (handler == null) {
            handler = (MessageHandler<T, T1>) MessageHandler.build(type);
            handlers.put(type, handler);
        }

        return handler;
    }
}
