package com.geni.backend.workflow.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "workflow_step_runs",
        indexes = {
                @Index(name = "idx_step_run_run_id",  columnList = "workflow_run_id"),
                @Index(name = "idx_step_run_step_id", columnList = "step_id")
        }
)
@Getter
@Setter
public class WorkflowStepRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_run_id", nullable = false)
    private WorkflowRun workflowRun;

    // ── Denormalized step identity ─────────────────────────────────────────────
    // Copied from WorkflowStep at execution time so run history stays accurate
    // even if the workflow definition is later edited.

    /**
     * clientId of the WorkflowStep that was executed.
     * Used to match step runs back to steps when building rerun context.
     */
    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    /** Copied from WorkflowStep.name at execution time. */
    @Column(name = "step_name", nullable = false)
    private String stepName;

    /** Copied from WorkflowStep.actionDefinitionId at execution time. */
    @Column(name = "action_definition_id", nullable = false)
    private String actionDefinitionId;

    // ── Status ─────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStepRunStatus status = WorkflowStepRunStatus.RUNNING;

    // ── Execution data ─────────────────────────────────────────────────────────

    /**
     * The actual values passed to the ActionExecutor after field mapping
     * resolution. Stored so the user can inspect exactly what was sent
     * and override values for a rerun.
     *
     * e.g. { "to": "jatin@gmail.com", "subject": "Bug: crash on login" }
     *
     * This is the runtime truth — not the template expressions from the definition.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resolved_inputs", columnDefinition = "jsonb")
    private Map<String, Object> resolvedInputs;

    /**
     * What the ActionExecutor returned on success.
     * Available to downstream steps via {{steps.<stepId>.output.field}}.
     * Null if step failed.
     *
     * e.g. { "messageId": "abc123", "threadId": "xyz456" }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output", columnDefinition = "jsonb")
    private Map<String, Object> output;

    /**
     * Error message if the step failed. Null on success.
     * Stored as TEXT not JSONB — raw exception message, not structured.
     */
    @Column(name = "error", columnDefinition = "text")
    private String error;

    /**
     * Number of attempts made. 1 = succeeded or failed on first try.
     * > 1 means retries were attempted before final success or failure.
     */
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 1;

    /**
     * True if this step's inputs were overridden by the user at rerun time
     * rather than resolved from field mapping templates.
     */
    @Column(name = "input_overridden", nullable = false)
    private boolean inputOverridden = false;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    // ── Helpers ────────────────────────────────────────────────────────────────

    public void markSuccess(Map<String, Object> output) {
        this.status      = WorkflowStepRunStatus.SUCCESS;
        this.output      = output;
        this.finishedAt  = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status     = WorkflowStepRunStatus.FAILED;
        this.error      = errorMessage;
        this.finishedAt = Instant.now();
    }

    public void markSkipped() {
        this.status     = WorkflowStepRunStatus.SKIPPED;
        this.finishedAt = Instant.now();
    }
}
