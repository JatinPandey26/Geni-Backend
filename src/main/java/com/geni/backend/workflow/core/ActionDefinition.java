package com.geni.backend.workflow.core;

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
public class ActionDefinition {

    // ── Identity ──────────────────────────────────────────────────────

    private final ActionType type;
    // Unique key — e.g. "SLACK_POST_MESSAGE", "GITHUB_CREATE_ISSUE", "CORE_HTTP_REQUEST"
    // Used as WorkflowStep.actionDefinitionId
    // Convention: {CONNECTOR_TYPE}_{VERB}_{NOUN}
    //             CORE_ prefix for built-in actions that need no integration

    private final String displayName;
    // "Post message" — shown in the step builder when user picks an action

    private final String description;
    // Longer explanation shown in the UI. e.g. "Send a message to a Slack channel."

    @NonNull
    private final ConnectorType connectorType;
    // For connector actions: SLACK, GITHUB, JIRA …
    // For built-in actions:  CORE  (add CORE to your ConnectorType enum)

    // ── Integration requirement ───────────────────────────────────────

    private final boolean requiresIntegration;
    // true  → user must supply an integrationId when adding this step
    //         (SLACK_POST_MESSAGE, GITHUB_CREATE_ISSUE, …)
    // false → built-in action, runs inside the engine with no external auth
    //         (CORE_TRANSFORM, CORE_DELAY, CORE_HTTP_REQUEST, …)

    // ── Schemas ───────────────────────────────────────────────────────

    private final Map<String, FieldSchema> inputSchema;
    // Fields the user must supply when configuring this step.
    // Each value is a template expression resolved at runtime:
    //   "{{trigger.email}}", "{{steps.<uuid>.output.text}}", or a literal.
    //
    // Examples:
    //   SLACK_POST_MESSAGE   → { channel: string(required), text: string(required) }
    //   CORE_DELAY           → { delayMs: number(required) }
    //   CORE_HTTP_REQUEST    → { url: string(required), method: string(required),
    //                            body: object(optional), headers: object(optional) }

    private final Map<String, FieldSchema> outputSchema;
    // Shape of data this action returns after execution.
    // Available downstream as {{steps.<stepId>.output.<key>}}.
    //
    // Examples:
    //   SLACK_POST_MESSAGE   → { ts: string, channel: string }
    //   GITHUB_CREATE_ISSUE  → { id: number, number: number, url: string, title: string }
    //   CORE_HTTP_REQUEST    → { status: number, body: object, headers: object }
    //   CORE_DELAY           → {}  (empty — nothing useful to return)

    // ── Domain helpers ────────────────────────────────────────────────

//    public boolean isBuiltIn() {
//        return connectorType == ConnectorType.CORE;
//    }

    public boolean hasInputField(String key) {
        return inputSchema != null && inputSchema.containsKey(key);
    }

    public boolean hasOutputField(String key) {
        return outputSchema != null && outputSchema.containsKey(key);
    }
}