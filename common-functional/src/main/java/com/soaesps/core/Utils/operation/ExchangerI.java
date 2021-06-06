package com.soaesps.core.Utils.operation;

public interface ExchangerI<T> {
    T getValue();

    default T getValue(HandlerI handler) {
        T value = getValue();
        if (value == null) {
            addToProcessQueue(handler);
        }

        return value;
    }

    void addToProcessQueue(HandlerI handler);

    HandlerI<T> getNextHandler();

    boolean hasNextHandler();

    default void updateValue(T value) {
        save(value);
        while (hasNextHandler()) {
            executeNextHandler();
        }
    }

    default void executeNextHandler() {
        HandlerI<T> handler = getNextHandler();
        if (handler != null) {
            handler.process(this);
        }
    }

    void save(T value);
}