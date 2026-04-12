package com.geni.backend.workflow.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolves field mapping template expressions against an ExecutionContext.
 *
 * Template syntax:
 *   {{trigger.<field>}}                          → trigger payload field
 *   {{steps.<stepClientId>.output.<field>}}      → step output field
 *   Mixed: "Hello {{trigger.name}}, issue #{{trigger.issue.number}}"
 *   Literal: "#inbox-summaries"                  → passed through as-is
 *
 * Resolution rules:
 *   - All {{ }} expressions in a value are replaced in a single pass
 *   - Missing fields resolve to empty string — never throw
 *   - Nested paths (user.email) are walked recursively into nested maps
 *   - Non-string values (Integer, Boolean, List) are coerced via toString()
 *
 * Example:
 *   template:  "Bug from {{trigger.sender.login}}: {{steps.abc-123.output.summary}}"
 *   context:   trigger={ sender.login: "jatin" }, steps={ abc-123: { summary: "crash" } }
 *   result:    "Bug from jatin: crash"
 */
@Slf4j
@Component
public class FieldMappingResolver {

    // Matches {{ anything }} — greedy false so nested braces don't cause issues
    private static final Pattern EXPR = Pattern.compile("\\{\\{([^}]+?)}}");

    private static final String TRIGGER_PREFIX = "trigger.";
    private static final String STEPS_PREFIX   = "steps.";

    /**
     * Resolves all field mappings for a step.
     *
     * @param fieldMappings raw mappings from WorkflowStep
     *                      e.g. { "to": "{{trigger.sender.login}}@gmail.com", "subject": "Bug" }
     * @param context       current execution context
     * @return resolved map with all expressions replaced
     *                      e.g. { "to": "jatin@gmail.com", "subject": "Bug" }
     */
    public Map<String, Object> resolve(Map<String, Object> fieldMappings,
                                       ExecutionContext context) {
        if (fieldMappings == null || fieldMappings.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, Object> entry : fieldMappings.entrySet()) {
            Object resolvedValue = resolveExpressionTypeSpecific(entry.getValue(), context);
            resolved.put(entry.getKey(), resolvedValue);
            log.debug("Resolved mapping '{}': '{}' → '{}'",
                    entry.getKey(), entry.getValue(), resolvedValue);
        }
        return resolved;
    }

    /*
    *  Resolves expression based on type either String or List
    *  */
    private Object resolveExpressionTypeSpecific(Object value , ExecutionContext context){

        if (value instanceof String str) {
            return resolveExpression(str, context);
        }

        else if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).map(s -> resolveExpression(s,context)).collect(Collectors.toSet());
        }

        return Set.of();
    }

    /**
     * Resolves a single template string.
     * Replaces all {{ }} occurrences, leaves plain text untouched.
     */
    public String resolveExpression(String template, ExecutionContext context) {
        if (template == null) return "";
        if (!template.contains("{{")) return template; // fast path — no expressions

        Matcher matcher = EXPR.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String path  = matcher.group(1).trim();
            String value = resolvePath(path, context);
            // escape $ and \ so they're treated as literals in the replacement string
            matcher.appendReplacement(result,
                    Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    // ── Path resolution ───────────────────────────────────────────────────────

    /**
     * Resolves a single path expression to its string value.
     *
     * Supported forms:
     *   trigger.<field>                       — flat trigger payload key
     *   trigger.<nested>.<field>              — nested path walked recursively
     *   steps.<stepClientId>.output.<field>   — step output field
     *
     * Returns empty string for any missing/null value.
     */
    private String resolvePath(String path, ExecutionContext context) {
        try {
            if (path.startsWith(TRIGGER_PREFIX)) {
                return resolveTriggerPath(path.substring(TRIGGER_PREFIX.length()), context);
            }

            if (path.startsWith(STEPS_PREFIX)) {
                return resolveStepPath(path.substring(STEPS_PREFIX.length()), context);
            }

            log.warn("Unknown expression prefix in path: '{}' — returning empty", path);
            return "";

        } catch (Exception e) {
            log.warn("Failed to resolve path '{}': {} — returning empty",
                    path, e.getMessage());
            return "";
        }
    }

    /**
     * Resolves trigger.* paths.
     *
     * Tries flat key first (most trigger payloads use dot-notation keys like "issue.title").
     * Falls back to nested map walk if flat key not found.
     *
     * e.g. trigger.issue.title →
     *   1. try context.triggerPayload.get("issue.title")   ← flat key, preferred
     *   2. try context.triggerPayload.get("issue").get("title")  ← nested walk
     */
    private String resolveTriggerPath(String fieldPath, ExecutionContext context) {
        Map<String, Object> triggerPayload = context.getTriggerPayload();

        // flat key lookup first — trigger payloads use dot-notation keys
        if (triggerPayload.containsKey(fieldPath)) {
            return toStr(triggerPayload.get(fieldPath));
        }

        // fallback: nested map walk
        Object value = walkPath(triggerPayload, fieldPath.split("\\."));
        return toStr(value);
    }

    /**
     * Resolves steps.<stepClientId>.output.<field> paths.
     *
     * Format: steps.3fa85f64-5717-4562-b3fc-2c963f66afa6.output.summary
     *                └── stepClientId (UUID) ──────────────┘       └─ field
     */
    private String resolveStepPath(String remainder, ExecutionContext context) {
        // remainder = "<uuid>.output.<field>"
        int firstDot = remainder.indexOf('.');
        if (firstDot == -1) {
            log.warn("Malformed step path — missing dot after UUID: '{}'", remainder);
            return "";
        }

        String uuidStr = remainder.substring(0, firstDot);
        String rest    = remainder.substring(firstDot + 1); // "output.<field>"

        UUID stepClientId;
        try {
            stepClientId = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            log.warn("Malformed step UUID in expression: '{}'", uuidStr);
            return "";
        }

        Map<String, Object> stepOutput = context.getStepOutput(stepClientId.toString());
        if (stepOutput.isEmpty()) {
            log.debug("Step {} has no output in context yet — returning empty", stepClientId);
            return "";
        }

        // flat key first, then nested walk
        if (stepOutput.containsKey(rest)) {
            return toStr(stepOutput.get(rest));
        }

        Object value = walkPath(stepOutput, rest.split("\\."));
        return toStr(value);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Walks a dot-separated path through nested maps.
     * Returns null if any segment is missing or not a Map.
     *
     * e.g. walkPath({ user: { email: "a@b.com" } }, ["user", "email"]) → "a@b.com"
     */
    @SuppressWarnings("unchecked")
    private Object walkPath(Map<String, Object> root, String[] segments) {
        Object current = root;
        for (String segment : segments) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(segment);
            if (current == null) return null;
        }
        return current;
    }

    /**
     * Converts any value to String safely.
     * null → empty string
     * List/Array → toString() (comma-joined via default Java toString)
     * Everything else → toString()
     */
    private String toStr(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}