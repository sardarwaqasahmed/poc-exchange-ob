package com.poc.baraka.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poc.baraka.common.GsonOffsetDateTime;

import java.time.OffsetDateTime;

/**
 * @author Waqas Ahmed
 */
public class JsonUtils {

    public static String toJSON(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(OffsetDateTime.class, new GsonOffsetDateTime()).create();
        return gson.toJson(object);
    }
}
