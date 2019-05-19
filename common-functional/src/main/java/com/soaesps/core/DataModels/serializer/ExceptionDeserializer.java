package com.soaesps.core.DataModels.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ExceptionDeserializer extends JsonDeserializer<Exception> {
    @Override
    public Exception deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException {
        return new Exception(arg0.getValueAsString());
    }
}