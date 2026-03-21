package com.geni.backend.workflow.service;

import com.geni.backend.workflow.core.CreateWorkflowRequest;
import com.geni.backend.workflow.core.WorkflowDefinitionResponse;
import com.geni.backend.workflow.enums.WorkflowStatus;

import java.util.List;
import java.util.UUID;

public interface WorkflowDefinitionService {

    WorkflowDefinitionResponse create(CreateWorkflowRequest request);

    List<WorkflowDefinitionResponse> listAll();

    WorkflowDefinitionResponse getById(UUID id);

    WorkflowDefinitionResponse update(UUID id, CreateWorkflowRequest request);

    WorkflowDefinitionResponse updateStatus(UUID id, WorkflowStatus status);

    void delete(UUID id);
}
