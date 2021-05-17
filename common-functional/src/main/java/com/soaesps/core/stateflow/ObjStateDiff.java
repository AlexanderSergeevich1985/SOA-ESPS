package com.soaesps.core.stateflow;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

abstract public class ObjStateDiff<T extends Serializable> implements FieldUpdaterI {
    public static final int UPDATE_INVALID = -2;

    public static final int UPDATER_INVALID = -1;

    public static final int UPDATE_SUCCESS = 0;

    static public Map<String, FieldUpdaterI> updaters = new IdentityHashMap<>();

    private Map<String, Object> updates = new IdentityHashMap<>();

    private T object;

    public ObjStateDiff() {}

    public <T> T getUpdate(String key, Class<T> tClass) {
        Object value = updates.get(key);

        return tClass.isInstance(value) ? tClass.cast(updates.get(key)) : null;
    }


    public void setUpdate(String key, Object value) {
        updates.put(key, value);
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public FieldUpdaterI getFieldUpdater(String fieldName, Class fieldClass) {
        FieldUpdaterI updater = updaters.get(fieldName);
        if (updater == null) {
            updater = getUpdater(fieldClass);
            updaters.put(fieldName, updater);
        }

        return updater;
    }

    public int updateField(String fieldName) {
        Object update = updates.get(fieldName);
        if (update == null) {
            return UPDATE_INVALID;
        }
        FieldUpdaterI updater = getFieldUpdater(fieldName, update.getClass());
        if (updater == null) {
            return UPDATER_INVALID;
        }
        updater.update(object, update);

        return UPDATE_SUCCESS;
    }
}