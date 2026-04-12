package com.geni.backend.workflow.validation;

import com.geni.backend.common.Schema;
import com.geni.backend.common.SchemaExtractor;
import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.integration.validator.IntegrationValidator;
import com.geni.backend.trigger.validator.TriggerDefinitionValidator;
import com.geni.backend.workflow.core.CreateWorkflowRequest;
import com.geni.backend.workflow.utils.PlaceHolderExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates reference validation for a workflow definition.
 *
 * Each individual validator lives in its own package and can be
 * called independently from anywhere in the codebase.
 *
 * This class just coordinates them in the right order for a workflow save.
 */
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class WorkflowReferenceValidator {

    private final TriggerDefinitionValidator triggerValidator;
    private final ActionDefinitionValidator  actionValidator;
    private final IntegrationValidator       integrationValidator;
    private AvailableContext availableContext;

    public void validate(CreateWorkflowRequest request) {
        availableContext=new AvailableContext();
        validateTrigger(request.getTrigger(),availableContext);
        List<CreateWorkflowRequest.StepRequest> sortedStepRequests = topologicalSort(request.getSteps());
        sortedStepRequests.forEach(s -> validateStep(s,availableContext));
    }

    // ── Trigger ───────────────────────────────────────────────────────────────

    private void validateTrigger(CreateWorkflowRequest.TriggerRequest trigger,AvailableContext availableContext) {

        // validate trigger configuration

        // 1. trigger definition exists + integration requirement is consistent
        var def = triggerValidator.validateWithIntegration(
                trigger.getTriggerDefinitionId(),
                trigger.getIntegrationId()
        ,     trigger.getConfig());

        // 2. if integration is required, check it exists and is active
        if (def.isRequiresIntegration()) {
            integrationValidator.validateActiveWithConnector(trigger.getIntegrationId(), String.valueOf(def.getConnectorType()));
        }

        def.getPayloadSchema().keySet().forEach(f -> {
            if(availableContext.triggerFields.contains(f)){
                throw new WorkflowValidationException("AvailableContextField cannot be duplicate . Field : " + f);
            }
            availableContext.triggerFields.add(f);
        });
    }

    // ── Steps ─────────────────────────────────────────────────────────────────

    private void validateStep(CreateWorkflowRequest.StepRequest step,AvailableContext availableContext) {
        // 1. action definition exists + integration requirement is consistent
        var def = actionValidator.validateWithIntegration(
                step.getName(),
                step.getActionDefinitionId(),
                step.getIntegrationId());

        if(def == null) {
            throw new IllegalStateException("Action handler should never be null here because validateWithIntegration should throw if it doesn't exist");
        }

        // 2. if integration is required, check it exists and is active
        if (def.definition().isRequiresIntegration()) {
            integrationValidator.validateActiveWithConnector(step.getIntegrationId(), String.valueOf(def.definition().getConnectorType()));
        }

        validateFieldMappings(step, availableContext);


        Set<String> stepOutputs = new HashSet<>();
        def.definition().getOutputSchema().keySet().forEach(s -> {
            if(stepOutputs.contains(s)){
                throw new WorkflowValidationException("Step cannot have duplicate outputs. Step : " + def.getDefinition().getDisplayName() + " , output field : " + s);
            }
            stepOutputs.add(s);
        });

        // 🔥 ADD THIS STEP OUTPUT TO CONTEXT
        availableContext.stepOutputs.put(
                step.getId(),
                stepOutputs
        );
    }

    private void validateFieldMappings(CreateWorkflowRequest.StepRequest step, AvailableContext context) {

        if (step.getFieldMappings() == null) return;

        for (Object value : step.getFieldMappings().values()) {

            List<String> expressions = PlaceHolderExtractor.extract(value);

            for (String expr : expressions) {
                validateExpression(expr, step, context);
            }
        }
    }

    private void validateExpression(String expr,
                                    CreateWorkflowRequest.StepRequest currentStep,
                                    AvailableContext context) {

        if (expr.startsWith("trigger.")) {
            validateIfExistsTriggerPayload(expr.substring(8), context);
        }
        else if (expr.startsWith("steps.")) {
            validateIfExistsInParentSteps(expr.substring(6), currentStep, context);
        }
        else {
            throw new WorkflowValidationException("Invalid expression root: " + expr);
        }
    }

    private void validateIfExistsTriggerPayload(String path, AvailableContext context) {

        if (!context.triggerFields.contains(path)) {
            throw new WorkflowValidationException("Invalid trigger field: " + path);
        }
    }

    private void validateIfExistsInParentSteps(String expr,
                                       CreateWorkflowRequest.StepRequest currentStep,
                                       AvailableContext context) {

        String[] parts = expr.split("\\.");

        UUID stepId = UUID.fromString(parts[0]);


        if (!context.stepOutputs.containsKey(stepId.toString())) {
            throw new WorkflowValidationException(
                    "Step not available in context (must be parent or ancestor): " + stepId);
        }

        String field = parts[1];

        if (!context.stepOutputs.get(stepId.toString()).contains(field)) {
            throw new WorkflowValidationException(
                    "Invalid field '" + field + "' for step " + stepId);
        }
    }

    private List<CreateWorkflowRequest.StepRequest> topologicalSort(List<CreateWorkflowRequest.StepRequest> steps) {

        Map<String, CreateWorkflowRequest.StepRequest> map = steps.stream()
                .collect(Collectors.toMap((CreateWorkflowRequest.StepRequest::getId), Function.identity()));

        List<CreateWorkflowRequest.StepRequest> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (CreateWorkflowRequest.StepRequest step : steps) {
            dfs(step, map, visited, result);
        }

        return result;
    }

    private void dfs(CreateWorkflowRequest.StepRequest step,
                     Map<String, CreateWorkflowRequest.StepRequest> map,
                     Set<String> visited,
                     List<CreateWorkflowRequest.StepRequest> result) {

        if (visited.contains(step.getId())) return;

        if (step.getParentStepId() != null) {
            dfs(map.get(step.getParentStepId()), map, visited, result);
        }

        visited.add(step.getId());
        result.add(step);
    }
}
