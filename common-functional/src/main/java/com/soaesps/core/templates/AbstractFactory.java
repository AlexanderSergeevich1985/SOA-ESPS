package com.soaesps.core.templates;

public interface AbstractFactory<T> {
    T create(final String type);
}