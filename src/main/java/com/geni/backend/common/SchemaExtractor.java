package com.geni.backend.common;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SchemaExtractor {

    public static Schema extract(Class<?> clazz) {
        Map<String, FieldSchema> schema = new HashMap<>();
        extractRecursive("", clazz, schema);
        return new Schema(schema, clazz);
    }

    private static void extractRecursive(String prefix, Class<?> clazz, Map<String, FieldSchema> schema) {

        for (Field field : clazz.getDeclaredFields()) {

            String fieldName = field.getName();
            String fullPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            Class<?> type = field.getType();

            if (isPrimitive(type)) {
                schema.put(fullPath, FieldSchema.string(fieldName));
            }
            else if (Collection.class.isAssignableFrom(type)) {
                Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Class<?> itemClass = (Class<?>) genericType;

                extractRecursive(fullPath + "[]", itemClass, schema);
            }
            else {
                extractRecursive(fullPath, type, schema);
            }
        }
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.equals(String.class)
                || type.equals(Integer.class)
                || type.equals(Long.class)
                || type.equals(Boolean.class)
                || type.equals(Double.class)
                || type.equals(Instant.class);
    }
}
