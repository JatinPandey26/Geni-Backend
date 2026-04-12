package com.geni.backend.workflow.validation;

import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.workflow.core.ConditionDefinition;
import com.geni.backend.workflow.core.CreateWorkflowRequest;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Validates the workflow tree at save time before any persistence.
 *
 * Checks (in order):
 *  1. All step ids are non-blank and unique within the request
 *  2. No step references itself as its own parent
 *  3. Every getParentStepId references a step that exists in the request
 *  4. At least one root step exists (getParentStepId = null)
 *  5. No cycles — the parent_step_id chain never revisits a node
 *  6. All steps are reachable from a root (no disconnected islands)
 *  7. Field mapping step references point only to strict ancestors
 *  8. Edge conditions are syntactically valid
 */
@Component
public class DagValidator {

    // Matches {{steps.<uuid>.output.anything}} — may appear multiple times per value
    private static final Pattern STEP_REF =
            Pattern.compile("\\{\\{steps\\.([0-9a-fA-F\\-]{36})\\.output\\.[^}]+}}");

    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    public void validate(List<CreateWorkflowRequest.StepRequest> steps) {

        // ── 1. Unique, non-blank ids ──────────────────────────────────────
        Set<String> ids = new HashSet<>();
        for (var step : steps) {
            if (step.getId() == null || step.getId().isBlank()) {
                throw new WorkflowValidationException("Every step must have a non-blank id");
            }
            if (!ids.add(step.getId())) {
                throw new WorkflowValidationException("Duplicate step id: " + step.getId());
            }
        }

        // ── 2. No self-reference ──────────────────────────────────────────
        for (var step : steps) {
            if (step.getId().equals(step.getParentStepId())) {
                throw new WorkflowValidationException(
                        "Step '" + step.getName() + "' references itself as parent");
            }
        }

        // ── 3. All getParentStepIds reference known steps ────────────────────
        for (var step : steps) {
            if (step.getParentStepId() != null && !ids.contains(step.getParentStepId())) {
                throw new WorkflowValidationException(
                        "Step '" + step.getName() + "' has getParentStepId '" + step.getParentStepId()
                                + "' which does not exist in this workflow");
            }
        }

        // ── 4. At least one root ──────────────────────────────────────────
        boolean hasRoot = steps.stream().anyMatch(s -> s.getParentStepId() == null);
        if (!hasRoot) {
            throw new WorkflowValidationException(
                    "Workflow must have at least one root step (getParentStepId = null)");
        }

        // Build adjacency: parent → [children]
        Map<String, List<String>> children = new HashMap<>();
        Map<String, String> parentOf       = new HashMap<>();
        ids.forEach(id -> children.put(id, new ArrayList<>()));

        for (var step : steps) {
            if (step.getParentStepId() != null) {
                children.get(step.getParentStepId()).add(step.getId());
                parentOf.put(step.getId(), step.getParentStepId());
            }
        }

        // ── 5. No cycles (DFS from every root) ───────────────────────────
        Set<String> roots = steps.stream()
                .filter(s -> s.getParentStepId() == null)
                .map(CreateWorkflowRequest.StepRequest::getId)
                .collect(Collectors.toSet());

        Set<String> visited  = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        for (var root : roots) {
            dfsCycleCheck(root, children, visited, recStack);
        }

        // ── 6. All steps reachable from a root ────────────────────────────
        Set<String> reachable = new HashSet<>();
        Queue<String> queue   = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            var curr = queue.poll();
            if (reachable.add(curr)) {
                queue.addAll(children.getOrDefault(curr, List.of()));
            }
        }
        Set<String> unreachable = new HashSet<>(ids);
        unreachable.removeAll(reachable);
        if (!unreachable.isEmpty()) {
            var names = steps.stream()
                    .filter(s -> unreachable.contains(s.getId()))
                    .map(CreateWorkflowRequest.StepRequest::getName)
                    .collect(Collectors.joining(", "));
            throw new WorkflowValidationException(
                    "Unreachable steps (not connected to any root): " + names);
        }

        // ── 7. Field mapping ancestry check ──────────────────────────────
        for (var step : steps) {
            if (step.getFieldMappings() == null) continue;
            Set<String> ancestors = ancestors(step.getId(), parentOf);
            for (var entry : step.getFieldMappings().entrySet()) {
                Set<String> refs = getRefs(entry.getValue());
                for(String ref : refs){
                    var matcher = STEP_REF.matcher(ref);
                    while (matcher.find()) {
                        String refId = matcher.group(1);
                        if (!ancestors.contains(refId)) {
                            throw new WorkflowValidationException(
                                    "Step '" + step.getName() + "': field mapping '" + entry.getKey()
                                            + "' references step '" + refId
                                            + "' which is not an ancestor of this step. "
                                            + "Only ancestors are guaranteed to have completed.");
                        }
                    }
                }
            }
        }

        // ── 8. Condition syntax validation ────────────────────────────────
        for (var step : steps) {
            if (step.getEdgeCondition() == null) continue;
            validateCondition(step.getName(), step.getEdgeCondition());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Set<String> getRefs(Object value){
        if (value == null) {
            throw new WorkflowValidationException("Ref cannot be null");
        }

        if (value instanceof String str) {
           return Set.of(str);
        }

        else if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toSet());
        }

        throw new WorkflowValidationException("Type of ref is not supported");
    }

    private void dfsCycleCheck(String node,
                               Map<String, List<String>> children,
                               Set<String> visited,
                               Set<String> recStack) {
        visited.add(node);
        recStack.add(node);
        for (var child : children.getOrDefault(node, List.of())) {
            if (!visited.contains(child)) {
                dfsCycleCheck(child, children, visited, recStack);
            } else if (recStack.contains(child)) {
                throw new WorkflowValidationException(
                        "Cycle detected in workflow tree at step id: " + child);
            }
        }
        recStack.remove(node);
    }

    /** Walk parent_step_id links upward and collect all ancestor ids. */
    private Set<String> ancestors(String stepId, Map<String, String> parentOf) {
        Set<String> result = new HashSet<>();
        String cur = parentOf.get(stepId);
        while (cur != null) {
            result.add(cur);
            cur = parentOf.get(cur);
        }
        return result;
    }

    private void validateCondition(String stepName, ConditionDefinition condition) {

        if (condition instanceof ConditionDefinition.StructuredCondition sc) {

            if (sc.field() == null || sc.field().isBlank()) {
                throw new WorkflowValidationException(
                        "Step '" + stepName + "': structured condition must have a field");
            }
            if (sc.operator() == null) {
                throw new WorkflowValidationException(
                        "Step '" + stepName + "': structured condition must have an operator");
            }
            boolean needsValue =
                    sc.operator() != ConditionDefinition.StructuredCondition.Operator.IS_NULL
                            && sc.operator() != ConditionDefinition.StructuredCondition.Operator.IS_NOT_NULL;
            if (needsValue && sc.value() == null) {
                throw new WorkflowValidationException(
                        "Step '" + stepName + "': operator " + sc.operator() + " requires a value");
            }

        } else if (condition instanceof ConditionDefinition.SpelCondition sc) {

            if (sc.expression() == null || sc.expression().isBlank()) {
                throw new WorkflowValidationException(
                        "Step '" + stepName + "': SpEL condition must have a non-blank expression");
            }
            try {
                spelParser.parseExpression(sc.expression());
            } catch (Exception e) {
                throw new WorkflowValidationException(
                        "Step '" + stepName + "': invalid SpEL expression — " + e.getMessage());
            }

        } else {
            throw new WorkflowValidationException(
                    "Step '" + stepName + "': unknown condition triggerType " + condition.getClass().getSimpleName());
        }
    }

}