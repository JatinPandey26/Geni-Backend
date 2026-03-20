package com.geni.backend.common;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

// FieldSchema.java
// Describes one field in a node's input or output schema
@Value
@Builder
public class FieldSchema {
    String type;         // "string", "number", "boolean", "object"
    boolean required;
    String description;
    Map<String, FieldSchema> properties;  // non-null when type = "object"

    public static FieldSchema string(String description) {
        return FieldSchema.builder().type("string").required(true)
                .description(description).build();
    }

    public static FieldSchema optionalString(String description) {
        return FieldSchema.builder().type("string").required(false)
                .description(description).build();
    }

    public static FieldSchema bool(String description) {
        return FieldSchema.builder().type("boolean").required(false)
                .description(description).build();
    }
}
