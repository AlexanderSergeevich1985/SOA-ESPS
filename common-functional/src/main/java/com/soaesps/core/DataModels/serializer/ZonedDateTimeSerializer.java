package com.soaesps.core.DataModels.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
    @Override
    public void serialize(ZonedDateTime dateTime, JsonGenerator generator, SerializerProvider provider) throws IOException {
        String dateTimeString = dateTime.format(DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
        generator.writeString(dateTimeString);
    }
}