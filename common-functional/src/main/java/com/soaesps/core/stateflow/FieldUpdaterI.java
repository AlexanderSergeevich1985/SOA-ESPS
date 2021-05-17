package com.soaesps.core.stateflow;

import java.io.Serializable;

public interface FieldUpdaterI extends Serializable {
    void update(Object obj, Object update);

    FieldUpdaterI getUpdater(Class clazz);
}