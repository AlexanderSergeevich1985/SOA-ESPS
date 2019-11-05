package com.soaesps.core.Utils.transaction;

public interface TransDesc<T extends Number> {
    T getValue();

    enum Type {
        AUGMENTED(0),
        REDUCED(1);

        private int value;

        Type(final int value) {
            this.value = value;
        }
    }
}