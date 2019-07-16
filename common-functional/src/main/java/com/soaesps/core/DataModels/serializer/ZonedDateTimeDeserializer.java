package com.soaesps.core.DataModels.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
    private static final String NULL_VALUE = "null";

    @Override
    public ZonedDateTime deserialize(JsonParser jp, DeserializationContext decxt) throws IOException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        String dateString = node.textValue();

        ZonedDateTime dateTime = null;
        if (!NULL_VALUE.equals(dateString)) {
            dateTime = ZonedDateTime.parse(dateString, ISO_DATE_TIME);
        }
        return dateTime;
    }
}