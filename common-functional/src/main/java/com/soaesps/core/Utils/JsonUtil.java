package com.soaesps.core.Utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonUtil {
    static final int DEFAULT_BUFF_SIZE = 2048;

    public static final String DEFAULT_ENCODING = "utf-8";

    private static final Logger logger = Logger.getLogger(JsonUtil.class.getName());

    static public final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        //MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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

    @Nullable
    static public String byteToString(final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        String json = new String(data, StandardCharsets.UTF_8);
        if (json.isEmpty()) {
            return null;
        }

        return json;
    }
}