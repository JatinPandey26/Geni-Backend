package com.geni.backend.workflow.repository;

import com.geni.backend.workflow.core.WorkflowDefinition;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import com.geni.backend.workflow.enums.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ── WorkflowDefinitionRepository ────────────────────────────────────────────

@Repository
public interface WorkflowDefinitionRepository
        extends JpaRepository<WorkflowDefinition, UUID> , JpaSpecificationExecutor<WorkflowDefinition> {

    List<WorkflowDefinition> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<WorkflowDefinition> findByIdAndUserId(UUID id, String userId);

    List<WorkflowDefinition> findByTriggerTypeAndStatus(
            String triggerType, WorkflowStatus status);


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
    where triggerType = :triggerType
    AND (:requiresIntegration = false OR w.triggerIntegrationId IS NOT NULL)  
    """)
    List<WorkflowTriggerView> findByTriggerType(
            @Param("triggerType") String triggerType,
            @Param("requiresIntegration") boolean requiresIntegration);

    @Query("UPDATE WorkflowDefinition w SET w.triggerIntegrationId = NULL WHERE w.triggerIntegrationId = :integrationId")
    int clearIntegrationId(@Param("integrationId") Long integrationId);

}
