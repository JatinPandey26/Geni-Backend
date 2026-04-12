package com.geni.backend.workflow.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Sealed hierarchy for edge conditions stored as JSONB.
 *
 * Discriminator field: "triggerType"
 *   STRUCTURED → StructuredCondition
 *   SPEL       → SpelCondition
 *
 * Stored on WorkflowStep.edgeCondition.
 * Null edgeCondition = unconditional edge (always fires).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConditionDefinition.StructuredCondition.class, name = "STRUCTURED"),
        @JsonSubTypes.Type(value = ConditionDefinition.SpelCondition.class,       name = "SPEL")
})
public sealed interface ConditionDefinition
        permits ConditionDefinition.StructuredCondition,
        ConditionDefinition.SpelCondition {

    // ── STRUCTURED ─────────────────────────────────────────────────────────
    // Simple field/operator/value triple. UI renders as dropdowns.
    // field examples:
    //   "trigger.status"
    //   "steps.3fa85f64-5717-4562-b3fc-2c963f66afa6.output.amount"

    record StructuredCondition(
            String field,
            Operator operator,
            Object value          // String, Number, Boolean, or List for IN/NOT_IN
    ) implements ConditionDefinition {

        public enum Operator {
            EQ, NEQ,
            GT, GTE, LT, LTE,
            CONTAINS, NOT_CONTAINS,
            IS_NULL, IS_NOT_NULL,
            IN, NOT_IN,
            ANY_MATCH, ALL_MATCH,NO_MATCH  // for array fields, e.g. "trigger.tags"
        }
    }

    // ── SPEL ────────────────────────────────────────────────────────────────
    // Power-user escape hatch. Validated by SpelExpressionParser at save time.
    // Context variables available at runtime:
    //   #trigger   — trigger payload map
    //   #steps     — Map<String stepId, StepResult>
    //
    // Example:
    //   "#steps['3fa85f64'].output.amount > 1000 && #trigger.region == 'US'"

    record SpelCondition(
            String expression
    ) implements ConditionDefinition {}
}