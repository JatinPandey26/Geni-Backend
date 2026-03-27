package com.geni.backend.workflow.core;

import com.geni.backend.workflow.enums.WorkflowStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "workflow_definitions"
//        indexes = {
//                // Poller query: "all active workflows for trigger X"
//                @Index(name = "idx_wf_trigger_status",
//                        columnList = "trigger_definition_id, status")
//        }
)
@Data
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //TODO : change to userId of triggerType Long after authentication is implemented as foreign key to users table
    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status = WorkflowStatus.DRAFT;

    // ── Trigger (embedded columns) ─────────────────────────────
    @Column(name = "triggerType", nullable = false)
    private String triggerType;

    @Column(name = "trigger_integration_id")
    private Long triggerIntegrationId;

    /**
     * User-supplied config values matching TriggerDefinition.configSchema.
     * Stored as JSONB. e.g. {"labelFilter": "INBOX", "pollIntervalMinutes": 5}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trigger_config", columnDefinition = "jsonb")
    private Map<String, Object> triggerConfig = new HashMap<>();

    // ── Steps ──────────────────────────────────────────────────
    @OneToMany(
            mappedBy    = "workflowDefinition",
            cascade     = CascadeType.ALL,
            orphanRemoval = true,
            fetch       = FetchType.EAGER     // always needed together
    )
    @OrderBy("stepOrder ASC")
    private List<WorkflowStep> steps = new ArrayList<>();

    // ── Audit ──────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Helpers ────────────────────────────────────────────────
    public void addStep(WorkflowStep step) {
        step.setWorkflowDefinition(this);
        steps.add(step);
    }

    public void replaceSteps(List<WorkflowStep> newSteps) {
        steps.clear();
        newSteps.forEach(s -> {
            s.setWorkflowDefinition(this);
            steps.add(s);
        });
    }


}