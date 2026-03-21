package com.geni.backend.workflow.repository;

import com.geni.backend.workflow.core.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository
        extends JpaRepository<WorkflowStep, UUID> {

    // Executor: all root steps for a workflow (parent is null = fires on trigger)
    List<WorkflowStep> findByWorkflowDefinitionIdAndParentStepIdIsNull(UUID workflowId);

    // Executor: all children of a completed step
    List<WorkflowStep> findByWorkflowDefinitionIdAndParentStepId(
            UUID workflowId, UUID parentStepId);
}