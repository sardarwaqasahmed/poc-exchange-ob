package com.poc.baraka.common;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
/**
 * @author Waqas Ahmed
 */
public class GsonOffsetDateTime implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String ofDtString = json.getAsString();
        return OffsetDateTime.parse(ofDtString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Override
    public JsonElement serialize(OffsetDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
