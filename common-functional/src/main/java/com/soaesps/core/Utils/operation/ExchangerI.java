package com.soaesps.core.Utils.operation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ExchangerI<T> {
    T getValue();

    default T getValue(Consumer<T> handler) {
        T value = getValue();
        if (value == null) {
            addToProcessQueue(handler);
        }

        return value;
    }

    default T getValue(Supplier<T> handler) {
        T value = getValue();
        if (value == null) {
            addToProcessQueue(handler);
        }

        return value;
    }

    void addToProcessQueue(Consumer<T> handler);

    void addToProcessQueue(Supplier<T> handler);

    Supplier<T> getNextSupplier();

    Consumer<T> getNextConsumer();

    boolean hasNextHandler();

    default void updateValue(T value) {
        save(value);
        while (hasNextHandler()) {
            executeNextHandler();
        }
    }

    default void executeNextHandler() {
        Supplier<T> supplier = getNextSupplier();
        if (supplier != null) {
            save(supplier.get());
        } else {
            Consumer<T> consumer = getNextConsumer();
            if (consumer != null) {
                consumer.accept(getValue());
            }
        }
    }

    void save(T value);
}