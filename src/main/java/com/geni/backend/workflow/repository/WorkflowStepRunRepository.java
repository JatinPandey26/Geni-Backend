package com.geni.backend.workflow.repository;

import com.geni.backend.workflow.core.WorkflowStepRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkflowStepRunRepository extends JpaRepository<WorkflowStepRun, UUID> {
}
