package com.geni.backend.trigger.core;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.FieldSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class TriggerDefinition {

    // ── Identity ──────────────────────────────────────────────────────

    private final String type;
    // "GITHUB_ISSUE_CREATED", "GITHUB_PR_OPENED", "SLACK_MESSAGE_RECEIVED"
    // used as key in WorkflowTrigger.triggerType
    // used to match incoming TriggerEvent.triggerType

    private final String displayName;
    // "Issue Created" — shown in UI when user picks a trigger

    private final String source;
    // "EXTERNAL"  → fires from webhook (GitHub, Jira, Slack)
    // "MANUAL"    → user clicks run
    // "CRON"      → scheduled expression
    // "EVENT"     → internal system event (future)

    @NonNull
    private final ConnectorType connectorType;

    // ── Schemas ───────────────────────────────────────────────────────

    private final Map<String, FieldSchema> configSchema;
    // fields user fills when configuring this trigger on a workflow
    // GITHUB_ISSUE_CREATED → { repo: string(required), label: string(optional) }
    // CRON                 → { expression: string(required) }
    // MANUAL               → {} empty

    private final Map<String, FieldSchema> payloadSchema;
    // shape of runtime data available in {{trigger.xxx}} expressions
    // GITHUB_ISSUE_CREATED → { issue.title, issue.number, issue.body, sender.login }
    // this is what node configs can reference at execution time

    // ── Connector link ────────────────────────────────────────────────

    private final boolean requiresIntegration;
    // true  → EXTERNAL triggers — need an integrationId when saving WorkflowTrigger
    // false → MANUAL, CRON — no integration needed

    // ── Domain helpers ────────────────────────────────────────────────

    public boolean isExternal() {
        return "EXTERNAL".equals(source);
    }

    public boolean isManual() {
        return "MANUAL".equals(source);
    }

    public boolean isCron() {
        return "CRON".equals(source);
    }

    public boolean hasConfigField(String key) {
        return configSchema != null && configSchema.containsKey(key);
    }

    public boolean hasPayloadField(String key) {
        return payloadSchema != null && payloadSchema.containsKey(key);
    }

    // ── Validation ────────────────────────────────────────────────────

//    public ValidationResult validateConfig(Map<String, Object> config) {
//        List<ValidationError> errors = new ArrayList<>();
//
//        if (configSchema == null || configSchema.isEmpty()) {
//            return ValidationResult.ok();
//        }
//
//        configSchema.forEach((key, schema) -> {
//            Object value = config != null ? config.get(key) : null;
//
//            if (schema.isRequired() && value == null) {
//                errors.add(ValidationError.required("config." + key, key));
//                return;
//            }
//
//            if (value != null && value.toString().isBlank() && schema.isRequired()) {
//                errors.add(ValidationError.blank("config." + key, key));
//            }
//        });
//
//        return ValidationResult.of(errors);
//    }
}