package com.geni.backend.workflow.core;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory data structure that holds all resolved data for one workflow run.
 *
 * Starts with the trigger payload and grows as each step completes.
 * Passed through the entire execution tree — every step reads from it
 * and writes its output back to it.
 *
 * Three sources of data:
 *   trigger.*      — from the incoming TriggerEvent payload
 *   steps.*        — from completed step outputs (added as execution progresses)
 *   overrides.*    — user-supplied inputs for a rerun (bypasses field mapping resolution)
 */
@Slf4j
public class ExecutionContext {

    // trigger payload: flat map e.g. { "issue.title": "Bug", "sender.login": "jatin" }
    private final Map<String, Object> triggerPayload;

    // stepClientId → output map from that step's executor
    private final Map<String, Map<String, Object>> stepOutputs;

    // stepClientId → user-supplied override inputs (rerun with modified data)
    private final Map<String, Map<String, Object>> inputOverrides;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Fresh run — starts with trigger payload only */
    public ExecutionContext(Map<String, Object> triggerPayload) {
        this.triggerPayload = triggerPayload != null
                ? triggerPayload : Collections.emptyMap();
        this.stepOutputs    = new HashMap<>();
        this.inputOverrides = new HashMap<>();
    }

    /** Rerun — pre-populate with previous step outputs and optional overrides */
    public ExecutionContext(Map<String, Object> triggerPayload,
                            Map<String, Map<String, Object>> previousOutputs,
                            Map<String, Map<String, Object>> inputOverrides) {
        this.triggerPayload = triggerPayload != null
                ? triggerPayload : Collections.emptyMap();
        this.stepOutputs    = new HashMap<>(previousOutputs != null
                ? previousOutputs : Collections.emptyMap());
        this.inputOverrides = new HashMap<>(inputOverrides != null
                ? inputOverrides : Collections.emptyMap());
    }

    // ── Write (called by engine as steps complete) ────────────────────────────

    public void addStepOutput(String stepClientId, Map<String, Object> output) {
        stepOutputs.put(stepClientId, output != null ? output : Collections.emptyMap());
        log.debug("Added output for step {}: {} fields", stepClientId,
                output != null ? output.size() : 0);
    }

    // ── Read (called by FieldMappingResolver and ConditionEvaluator) ──────────

    public Map<String, Object> getTriggerPayload() {
        return Collections.unmodifiableMap(triggerPayload);
    }

    public Map<String, Object> getStepOutput(String stepClientId) {
        return stepOutputs.getOrDefault(stepClientId, Collections.emptyMap());
    }

    public boolean hasStepOutput(UUID stepClientId) {
        return stepOutputs.containsKey(stepClientId);
    }

    // ── Override support (rerun with modified inputs) ─────────────────────────

    public boolean hasOverride(UUID stepClientId) {
        return inputOverrides.containsKey(stepClientId);
    }

    public Map<String, Object> getOverride(UUID stepClientId) {
        return inputOverrides.getOrDefault(stepClientId, Collections.emptyMap());
    }

    // ── Raw access for SpEL context population ────────────────────────────────

    /** Returns a snapshot of all step outputs keyed by stepClientId string. */
    public Map<String, Map<String, Object>> getAllStepOutputs() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        stepOutputs.forEach((id, output) -> result.put(id.toString(), output));
        return Collections.unmodifiableMap(result);
    }
}