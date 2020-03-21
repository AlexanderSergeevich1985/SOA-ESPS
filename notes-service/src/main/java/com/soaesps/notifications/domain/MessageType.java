package com.soaesps.notifications.domain;

public enum MessageType {
    SIMPLE_MESSAGE(SimpleMessage.class);

    private Class clazz;

    MessageType(final Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(final Class clazz) {
        this.clazz = clazz;
    }
}