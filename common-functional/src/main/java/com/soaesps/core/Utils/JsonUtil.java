package com.soaesps.core.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonUtil {
    private static final Logger logger = Logger.getLogger(JsonUtil.class.getName());

    static public final ObjectMapper MAPPER = new ObjectMapper();

    @Nullable
    static public <T> T fromString(final String string, final Class<T> clazz) {
        try {
            return string != null ? MAPPER.readValue(string, clazz) : null;
        }
        catch (final IOException ex) {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[JsonUtil/fromString]: {}", ex);
            }
        }
        return null;
    }

    @Nullable
    static public String toString(final Object value) {
        try {
            return value != null ? MAPPER.writeValueAsString(value) : null;
        }
        catch (final JsonProcessingException ex) {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[JsonUtil/toString]: {}", ex);
            }
        }
        return null;
    }

    @Nullable
    static public JsonNode toJsonNode(final String value) {
        try {
            return MAPPER.readTree(value);
        }
        catch (final IOException ex) {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[JsonUtil/toJsonNode]: {}", ex);
            }
        }

        return null;
    }

    static public <T> T clone(final T value) {
        return fromString(toString(value), (Class<T>) value.getClass());
    }
}