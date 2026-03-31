package com.geni.backend.workflow.core;

import com.geni.backend.trigger.core.TriggerSource;
import com.geni.backend.workflow.enums.WorkflowRunStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "workflow_runs",
        indexes = {
                @Index(name = "idx_run_workflow_id", columnList = "workflow_definition_id, status"),
                @Index(name = "idx_run_parent",      columnList = "parent_run_id")
        }
)
@Getter
@Setter
public class WorkflowRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowRunStatus status = WorkflowRunStatus.RUNNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false)
    private TriggerSource triggeredBy;

    /**
     * The raw trigger payload that started this run.
     * Stored so re-execution can replay from the same starting point
     * without needing the original webhook to fire again.
     * e.g. { "issue.title": "Bug", "sender.login": "jatin" }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trigger_payload", columnDefinition = "jsonb")
    private Map<String, Object> triggerPayload;

    /**
     * If this run is a rerun, points to the original run it was derived from.
     * Null for fresh runs triggered by webhook or manual.
     * Allows the UI to show the full retry chain for a given trigger event.
     */
    @Column(name = "parent_run_id")
    private UUID parentRunId;

    /**
     * For reruns: which step the re-execution started from.
     * Null for full reruns and fresh runs.
     * Steps before this step had their stored outputs injected into
     * the ExecutionContext instead of being re-executed.
     */
    @Column(name = "rerun_from_step_id")
    private UUID rerunFromStepId;

    @OneToMany(
            mappedBy      = "workflowRun",
            cascade       = CascadeType.ALL,
            orphanRemoval = true,
            fetch         = FetchType.EAGER
    )
    @OrderBy("startedAt ASC")
    private List<WorkflowStepRun> stepRuns = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    // ── Helpers ───────────────────────────────────────────────────────────────

    public void addStepRun(WorkflowStepRun stepRun) {
        stepRun.setWorkflowRun(this);
        stepRuns.add(stepRun);
    }

    public void markSuccess() {
        this.status     = WorkflowRunStatus.SUCCESS;
        this.finishedAt = Instant.now();
    }

    public void markFailed() {
        this.status     = WorkflowRunStatus.FAILED;
        this.finishedAt = Instant.now();
    }

    /**
     * PARTIAL = at least one branch succeeded, at least one failed
     * (only relevant when onError = CONTINUE and a step failed but run continued)
     */
    public void markPartial() {
        this.status     = WorkflowRunStatus.PARTIAL;
        this.finishedAt = Instant.now();
    }

    public boolean hasFailedSteps() {
        return stepRuns.stream()
                .anyMatch(s -> s.getStatus() == WorkflowStepRunStatus.FAILED);
    }
}