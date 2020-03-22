package com.soaesps.notifications.domain;

public class Message {
    private MessageType type;

    private String body;

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