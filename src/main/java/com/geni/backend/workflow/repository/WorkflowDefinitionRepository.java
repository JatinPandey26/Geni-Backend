package com.geni.backend.workflow.repository;

import com.geni.backend.workflow.core.WorkflowDefinition;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import com.geni.backend.workflow.enums.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ── WorkflowDefinitionRepository ────────────────────────────────────────────

@Repository
public interface WorkflowDefinitionRepository
        extends JpaRepository<WorkflowDefinition, UUID> {

    // Dashboard: all workflows for a user, newest first
    List<WorkflowDefinition> findByUserIdOrderByCreatedAtDesc(String userId);

    // Single workflow scoped to owner — prevents cross-user access
    Optional<WorkflowDefinition> findByIdAndUserId(UUID id, String userId);

    // Poller: all active workflows waiting for a given trigger
    List<WorkflowDefinition> findByTriggerTypeAndStatus(
            String triggerType, WorkflowStatus status);

    // Guard before deleting an integration:
    // "does any active workflow still use this integration as its trigger?"
    @Query("""
        SELECT COUNT(w) > 0 FROM WorkflowDefinition w
        WHERE w.userId = :userId
          AND w.triggerIntegrationId = :integrationId
          AND w.status = 'ACTIVE'
        """)
    boolean existsActiveWorkflowUsingTriggerIntegration(
            @Param("userId") String userId,
            @Param("integrationId") String integrationId);

    @Query("""
    SELECT 
      w.id as workflowId,
      w.name as workflowName,
      w.triggerType as triggerType,
      w.triggerConfig as triggerConfig
    FROM WorkflowDefinition w
    """)
    List<WorkflowTriggerView> findByTriggerType(String triggerType);
}
