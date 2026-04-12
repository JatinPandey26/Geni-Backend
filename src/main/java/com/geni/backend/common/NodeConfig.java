package com.geni.backend.common;

import com.geni.backend.workflow.core.ConditionDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeConfig {

    ConditionDefinition.StructuredCondition.Operator operator;

    Object value;

    boolean isRequired;

    FieldType type;
}