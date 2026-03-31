package com.geni.backend.workflow.core;

import com.geni.backend.common.NodeConfig;
import com.geni.backend.workflow.enums.OnErrorStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.w3c.dom.Node;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowRequest {

    @NotBlank
    String name;

    String description;

    @NotNull
    @Valid
    TriggerRequest trigger;

    @NotNull
    @Valid
    List<StepRequest> steps;

    // ── Nested: TriggerRequest ────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerRequest {

        @NotBlank
        String triggerDefinitionId;

        // nullable — MANUAL and CRON triggers don't need an integration
        Long integrationId;

        // user-supplied values matching TriggerDefinition.configSchema
        // e.g. { "repo": "my-org/my-repo", "labelFilter": "bug" }
        Map<String, NodeConfig> config;
    }

    // ── Nested: StepRequest ───────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepRequest {

        /**
         * Stable client-generated UUID for this step.
         * Must be unique within the workflow.
         *
         * Used by downstream steps in field mapping expressions:
         *   "{{steps.<this-id>.output.someField}}"
         *
         * Also referenced by other steps' parentStepId to build the tree.
         */
        @NotBlank
        String id;

        @NotBlank
        String name;

        /**
         * Parent step's id. null = root step, fires directly from trigger.
         * Must reference another step id present in this same request.
         */
        String parentStepId;

        /**
         * Gate condition on the edge from parent → this step.
         * null = unconditional (always fires when parent completes).
         *
         * Two shapes (discriminated by "triggerType"):
         *   STRUCTURED → { field, operator, value }
         *   SPEL       → { expression }
         *
         * Validated at save time; evaluated at runtime.
         */
        ConditionDefinition edgeCondition;

        // Human label shown in the visual builder. e.g. "Approved", "High value"
        String edgeLabel;

        @NotBlank
        String actionDefinitionId;

        // nullable — not required when ActionDefinition.requiresIntegration = false
        Long integrationId;

        // Keys = ActionDefinition.inputSchema field names
        // Values = template expressions: "{{trigger.email}}", "{{steps.<id>.output.text}}", or literals
        Map<String, String> fieldMappings;

        @NotNull
        OnErrorStrategy onError;

        // required only when onError = RETRY
        @Valid
        RetryConfigRequest retryConfig;
    }

    // ── Nested: RetryConfigRequest ────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryConfigRequest {

        @Min(1) @Max(10)
        int maxAttempts;

        @Min(100)
        long delayMs;

        @NotNull
        RetryConfig.BackoffStrategy backoff;
    }
}