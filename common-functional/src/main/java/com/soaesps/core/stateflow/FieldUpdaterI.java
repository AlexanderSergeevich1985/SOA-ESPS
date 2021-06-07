package com.soaesps.core.stateflow;

import java.io.Serializable;

public interface FieldUpdaterI<T> extends Serializable {
    T update(T obj);

    FieldUpdaterI getUpdater(Class clazz);
}