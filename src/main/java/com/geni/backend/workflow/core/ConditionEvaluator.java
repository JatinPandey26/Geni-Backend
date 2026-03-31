package com.geni.backend.workflow.core;

import com.geni.backend.workflow.core.ConditionDefinition.StructuredCondition;
import com.geni.backend.workflow.core.ConditionDefinition.StructuredCondition.*;
import com.geni.backend.workflow.core.ConditionDefinition.SpelCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Evaluates edge conditions against an ExecutionContext.
 *
 * Called by the engine after a parent step completes —
 * for each child step, evaluates its edgeCondition to decide
 * whether the child fires or is skipped.
 *
 * null condition = unconditional — always returns true.
 *
 * Two condition types:
 *
 *  STRUCTURED — field / operator / value triple.
 *    Field path follows the same syntax as field mappings:
 *      "trigger.issue.labels"
 *      "steps.<uuid>.output.status"
 *    Value is compared using the operator.
 *    Null-safe — missing fields evaluate to false, never throw.
 *
 *  SPEL — arbitrary Spring Expression Language expression.
 *    Context variables:
 *      #trigger — trigger payload Map<String, Object>
 *      #steps   — Map<String, Map<String, Object>> keyed by stepClientId string
 *    Uses SimpleEvaluationContext (read-only, no arbitrary method calls) for safety.
 *    Parse errors caught at save time (DagValidator). Runtime eval errors → false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionEvaluator {

    private final FieldMappingResolver fieldMappingResolver;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    /**
     * @param condition the edge condition (null = unconditional)
     * @param context   current execution context
     * @return true if the child step should fire, false if it should be skipped
     */
    public boolean evaluate(ConditionDefinition condition, ExecutionContext context) {
        if (condition == null) {
            return true; // unconditional edge — always fires
        }

        if (condition instanceof StructuredCondition sc) {
            return evaluateStructured(sc, context);
        }

        if (condition instanceof ConditionDefinition.SpelCondition sc) {
            return evaluateSpel(sc, context);
        }

        log.warn("Unknown condition type: {} — defaulting to false",
                condition.getClass().getSimpleName());
        return false;
    }

    // ── STRUCTURED ────────────────────────────────────────────────────────────

    private boolean evaluateStructured(StructuredCondition condition,
                                       ExecutionContext context) {
        // resolve the field path using the same resolver as field mappings
        // wrap in a template expression to reuse resolver's path logic
        String template = "{{" + condition.field() + "}}";
        String fieldValue = fieldMappingResolver.resolveExpression(template, context);

        log.debug("Structured condition: field='{}' resolved='{}' operator={} value='{}'",
                condition.field(), fieldValue, condition.operator(), condition.value());

        try {
            return applyOperator(fieldValue, condition.operator(), condition.value());
        } catch (Exception e) {
            log.warn("Structured condition evaluation failed for field '{}': {} — returning false",
                    condition.field(), e.getMessage());
            return false;
        }
    }

    private boolean applyOperator(String fieldValue, Operator operator, Object conditionValue) {
        return switch (operator) {

            case EQ ->
                    fieldValue.equals(toStr(conditionValue));

            case NEQ ->
                    !fieldValue.equals(toStr(conditionValue));

            case CONTAINS ->
                    fieldValue.contains(toStr(conditionValue));

            case NOT_CONTAINS ->
                    !fieldValue.contains(toStr(conditionValue));

            case IS_NULL ->
                    fieldValue.isEmpty();

            case IS_NOT_NULL ->
                    !fieldValue.isEmpty();

            case GT ->
                    toDouble(fieldValue) > toDouble(toStr(conditionValue));

            case GTE ->
                    toDouble(fieldValue) >= toDouble(toStr(conditionValue));

            case LT ->
                    toDouble(fieldValue) < toDouble(toStr(conditionValue));

            case LTE ->
                    toDouble(fieldValue) <= toDouble(toStr(conditionValue));

            case IN -> {
                List<String> values = toList(conditionValue);
                yield values.stream().anyMatch(fieldValue::equals);
            }

            case NOT_IN -> {
                List<String> values = toList(conditionValue);
                yield values.stream().noneMatch(fieldValue::equals);
            }
        };
    }

    // ── SPEL ──────────────────────────────────────────────────────────────────

    private boolean evaluateSpel(SpelCondition condition, ExecutionContext context) {
        try {
            // Build a read-only context — prevents arbitrary method calls
            // on Java classes. Users can access properties and use basic
            // operators but cannot call System.exit() or similar.
            var evalContext = SimpleEvaluationContext
                    .forReadOnlyDataBinding()
                    .withInstanceMethods()
                    .build();

            // Populate variables available in SpEL expressions:
            //   #trigger.issue.title
            //   #steps['abc-123'].output.summary
            evalContext.setVariable("trigger", context.getTriggerPayload());
            evalContext.setVariable("steps",   context.getAllStepOutputs());

            Boolean result = spelParser
                    .parseExpression(condition.expression())
                    .getValue(evalContext, Boolean.class);

            log.debug("SpEL condition '{}' evaluated to: {}", condition.expression(), result);
            return Boolean.TRUE.equals(result);

        } catch (EvaluationException e) {
            log.warn("SpEL condition evaluation failed for '{}': {} — returning false",
                    condition.expression(), e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String toStr(Object value) {
        return value == null ? "" : value.toString();
    }

    private double toDouble(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Cannot compare empty value as number");
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Cannot parse '" + value + "' as a number for numeric comparison");
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> toList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        if (value instanceof Collection<?> col) {
            return col.stream().map(Object::toString).toList();
        }
        // single value — wrap in list
        return List.of(toStr(value));
    }
}