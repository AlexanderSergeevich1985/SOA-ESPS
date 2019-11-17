package com.soaesps.aggregator.messages;

import com.soaesps.core.component.router.MessageI;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

public class BaseMessageConverter extends AbstractMessageConverter {
    public BaseMessageConverter() {
        super(new MimeType("text", "plain"));
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return (MessageI.class == clazz);
    }

    @Override
    protected Object convertFromInternal(final Message<?> message, final Class<?> targetClass, final Object conversionHint) {
        Object payload = message.getPayload();
        String text = payload instanceof String ? (String) payload : new String((byte[]) payload);

        return text;
    }
}