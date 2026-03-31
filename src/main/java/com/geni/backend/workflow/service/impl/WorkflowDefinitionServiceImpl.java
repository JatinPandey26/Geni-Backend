package com.geni.backend.workflow.service.impl;


import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.workflow.core.ActionHandlerRegistry;
import com.geni.backend.workflow.core.CreateWorkflowRequest;
import com.geni.backend.workflow.core.RetryConfig;
import com.geni.backend.workflow.core.RetryConfigResponse;
import com.geni.backend.workflow.core.StepResponse;
import com.geni.backend.workflow.core.TriggerResponse;
import com.geni.backend.workflow.core.WorkflowDefinition;
import com.geni.backend.workflow.core.WorkflowDefinitionResponse;
import com.geni.backend.workflow.core.WorkflowStep;
import com.geni.backend.workflow.enums.OnErrorStrategy;
import com.geni.backend.workflow.enums.WorkflowStatus;
import com.geni.backend.workflow.repository.WorkflowDefinitionRepository;
import com.geni.backend.workflow.service.WorkflowDefinitionService;
import com.geni.backend.workflow.validation.DagValidator;
import com.geni.backend.workflow.validation.WorkflowReferenceValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository repo;
    private final DagValidator dagValidator;
    private final WorkflowReferenceValidator workflowReferenceValidator;
    private final ActionHandlerRegistry actionRegistry;

    // UserContext will be injected here once the user layer is added:
    // private final UserContext userContext;

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    public WorkflowDefinitionResponse create(CreateWorkflowRequest request) {
        workflowReferenceValidator.validate(request);
        dagValidator.validate(request.getSteps());
        var wf = new WorkflowDefinition();
        // wf.setUserId(userContext.currentUserId());   // TODO uncomment when UserContext is ready
        applyRequest(wf, request);
        return toResponse(repo.save(wf));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionResponse> listAll() {
        // replace with repo.findByUserId(userContext.currentUserId()) once UserContext is ready
        return repo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDefinitionResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public WorkflowDefinitionResponse update(UUID id, CreateWorkflowRequest request) {
        workflowReferenceValidator.validate(request);
        dagValidator.validate(request.getSteps());

        var wf = findById(id);
        applyRequest(wf, request);
        return toResponse(repo.save(wf));
    }

    // ── Status patch ──────────────────────────────────────────────────────────

    @Override
    public WorkflowDefinitionResponse updateStatus(UUID id, WorkflowStatus status) {
        var wf = findById(id);
        wf.setStatus(status);
        return toResponse(repo.save(wf));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(UUID id) {
        repo.delete(findById(id));
    }

    // ── Private: lookup ───────────────────────────────────────────────────────

    private WorkflowDefinition findById(UUID id) {
        // scope to user once UserContext is ready:
        // return repo.findByIdAndUserId(id, userContext.currentUserId())
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Workflow not found: " + id));
    }

    // ── Private: request → entity ─────────────────────────────────────────────

    private void applyRequest(WorkflowDefinition wf, CreateWorkflowRequest req) {
        wf.setName(req.getName());
        wf.setDescription(req.getDescription());
        wf.setTriggerType(req.getTrigger().getTriggerDefinitionId());
        wf.setTriggerIntegrationId(req.getTrigger().getIntegrationId());
        wf.setTriggerConfig(
                req.getTrigger().getConfig() != null ? req.getTrigger().getConfig() :  Map.of());

        Set<WorkflowStep> steps = req.getSteps().stream()
                .map(this::toStepEntity)
                .collect(Collectors.toSet());
        wf.replaceSteps(steps.stream().toList());
    }

    private WorkflowStep toStepEntity(CreateWorkflowRequest.StepRequest req) {

        var actionDef = actionRegistry.findOrThrow(req.getActionDefinitionId());
        if (actionDef.definition().isRequiresIntegration()
                && (req.getIntegrationId() == null)) {
            throw new WorkflowValidationException(
                    "Step '" + req.getName() + "' uses action '" + req.getActionDefinitionId()
                            + "' which requires an integration, but no integrationId was provided.");
        }

        var step = new WorkflowStep();

        step.setClientId(UUID.fromString(req.getId()));   // client-supplied — stable reference
        // step.id is left null — Hibernate generates it on persist

        step.setName(req.getName());
        step.setParentStepId(
                req.getParentStepId() != null
                        ? UUID.fromString(req.getParentStepId())
                        : null);
        step.setEdgeCondition(req.getEdgeCondition());
        step.setEdgeLabel(req.getEdgeLabel());
        step.setActionDefinitionId(req.getActionDefinitionId());
        step.setIntegrationId(req.getIntegrationId());
        step.setFieldMappings(
                req.getFieldMappings() != null ? req.getFieldMappings() : Map.of());
        step.setOnError(req.getOnError());

        if (req.getOnError() == OnErrorStrategy.RETRY && req.getRetryConfig() != null) {
            var rc = new RetryConfig();
            rc.setMaxAttempts(req.getRetryConfig().getMaxAttempts());
            rc.setDelayMs(req.getRetryConfig().getDelayMs());
            rc.setBackoff(req.getRetryConfig().getBackoff());
            step.setRetryConfig(rc);
        }

        return step;
    }

    /** Injects the client-supplied UUID into the @Id field before persist. */
    private static void setId(WorkflowStep step, UUID id) {
        try {
            var field = WorkflowStep.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(step, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not set WorkflowStep id", e);
        }
    }

    // ── Private: entity → response ────────────────────────────────────────────

    private WorkflowDefinitionResponse toResponse(WorkflowDefinition wf) {
        var trigger = new TriggerResponse(
                wf.getTriggerType(),
                wf.getTriggerIntegrationId(),
                wf.getTriggerConfig()
        );

        var steps = wf.getSteps().stream()
                .map(this::toStepResponse)
                .toList();

        return new WorkflowDefinitionResponse(
                wf.getId(), wf.getName(), wf.getDescription(),
                wf.getStatus(), trigger, steps,
                wf.getCreatedAt(), wf.getUpdatedAt()
        );
    }

    private StepResponse toStepResponse(WorkflowStep s) {
        RetryConfigResponse rc = null;
        if (s.getRetryConfig() != null) {
            rc = new RetryConfigResponse(
                    s.getRetryConfig().getMaxAttempts(),
                    s.getRetryConfig().getDelayMs(),
                    s.getRetryConfig().getBackoff()
            );
        }
        return StepResponse.builder()
                .id(s.getClientId())           // expose clientId as "id" to the frontend
                .name(s.getName())
                .parentStepId(s.getParentStepId())
                .edgeCondition(s.getEdgeCondition())
                .edgeLabel(s.getEdgeLabel())
                .actionDefinitionId(s.getActionDefinitionId())
                .integrationId(s.getIntegrationId())
                .fieldMappings(s.getFieldMappings())
                .onError(s.getOnError())
                .retryConfig(rc)
                .build();
    }
}