package com.soaesps.notifications.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "message")
public class Message {
    @MongoId(FieldType.OBJECT_ID)
    private String messageId;

    @Indexed
    @Field("email")
    private String mailer;

    private MessageType type;

    private String body;

    public Message() {}

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    public String getMailer() {
        return mailer;
    }

    public void setMailer(final String mailer) {
        this.mailer = mailer;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(final MessageType type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public Class getClazz() {
        return type.getClazz();
    }
}