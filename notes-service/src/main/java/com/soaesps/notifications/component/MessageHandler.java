package com.soaesps.notifications.component;

import com.soaesps.notifications.domain.MessageType;
import com.soaesps.notifications.domain.SimpleMessage;

import javax.mail.internet.MimeMessage;

public interface MessageHandler<T, T1> {
    T handleMessage(T1 message);

    static MessageHandler build(final MessageType type) {
        switch (type) {
            case SIMPLE_MESSAGE:
                return new SimpleMessageHandler();
            default:
                return null;
        }
    }

    class SimpleMessageHandler implements MessageHandler<MimeMessage, SimpleMessage> {
        @Override
        public MimeMessage handleMessage(SimpleMessage message) {
            return null;
        }
    }
}