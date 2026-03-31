package com.geni.backend.common;

import com.geni.backend.workflow.core.ConditionDefinition;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

// FieldSchema.java
// Describes one field in a node's input or output schema
@Value
@Builder
public class FieldSchema {
    FieldType type;         // "string", "number", "boolean", "object"
    boolean required;
    String description;
    Map<String, FieldSchema> properties;  // non-null when triggerType = "object"
    List<ConditionDefinition.StructuredCondition.Operator> allowedOperators;

    public static FieldSchema string(String description) {
        return FieldSchema.builder().type(FieldType.STRING).required(true)
                .description(description).allowedOperators(List.of(ConditionDefinition.StructuredCondition.Operator.EQ)).build();
    }

    public static FieldSchema optionalString(String description) {
        return FieldSchema.builder().type(FieldType.STRING).required(false)
                .allowedOperators(List.of(ConditionDefinition.StructuredCondition.Operator.EQ))
                .description(description).build();
    }

    public static FieldSchema bool(String description) {
        return FieldSchema.builder().type(FieldType.BOOLEAN).required(false)
                .description(description).build();
    }
}
