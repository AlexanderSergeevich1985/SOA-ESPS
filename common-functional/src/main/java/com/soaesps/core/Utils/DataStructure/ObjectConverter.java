package com.soaesps.core.Utils.DataStructure;

import java.lang.reflect.ParameterizedType;

/**
 * Created by sniper on 19.11.22.
 */
public class ObjectConverter {
    static public <T> Class<T> getType(Object obj) {
        ParameterizedType type = (ParameterizedType) obj.getClass().getGenericSuperclass();

        return (Class<T>) type.getActualTypeArguments()[0];
    }

    public static <T> T cast(Object o, Class<T> clazz) {
        return clazz.isInstance(o) ? clazz.cast(o) : null;
    }
}