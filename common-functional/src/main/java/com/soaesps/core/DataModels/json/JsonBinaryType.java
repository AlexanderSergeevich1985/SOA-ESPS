package com.soaesps.core.DataModels.json;

import com.soaesps.core.Utils.JsonUtil;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.lang.reflect.ParameterizedType;

@Converter
public class JsonBinaryType<T> implements AttributeConverter<T, String> {
    private Class<T> clazz;

    @Nullable
    @Override
    public String convertToDatabaseColumn(final T entity) {
        return JsonUtil.toString(entity);
    }

    @Override
    public T convertToEntityAttribute(final String data) {
        return JsonUtil.fromString(data, getType());
    }

    public Class<T> getType() {
        if (clazz == null) {
            ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
            clazz = (Class<T>) type.getActualTypeArguments()[0];
        }

        return clazz;
    }
}