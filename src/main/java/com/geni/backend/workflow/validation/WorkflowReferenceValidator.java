package com.geni.backend.workflow.validation;

import com.geni.backend.integration.validator.IntegrationValidator;
import com.geni.backend.trigger.validator.TriggerDefinitionValidator;
import com.geni.backend.workflow.core.CreateWorkflowRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Orchestrates reference validation for a workflow definition.
 *
 * Each individual validator lives in its own package and can be
 * called independently from anywhere in the codebase.
 *
 * This class just coordinates them in the right order for a workflow save.
 */
@Component
@RequiredArgsConstructor
public class WorkflowReferenceValidator {

    private final TriggerDefinitionValidator triggerValidator;
    private final ActionDefinitionValidator  actionValidator;
    private final IntegrationValidator       integrationValidator;

    public void validate(CreateWorkflowRequest request) {
        validateTrigger(request.getTrigger());
        request.getSteps().forEach(this::validateStep);
    }

    // ── Trigger ───────────────────────────────────────────────────────────────

    private void validateTrigger(CreateWorkflowRequest.TriggerRequest trigger) {
        // 1. trigger definition exists + integration requirement is consistent
        var def = triggerValidator.validateWithIntegration(
                trigger.getTriggerDefinitionId(),
                trigger.getIntegrationId());

        // 2. if integration is required, check it exists and is active
        if (def.isRequiresIntegration()) {
            integrationValidator.validateActiveWithConnector(trigger.getIntegrationId(), String.valueOf(def.getConnectorType()));
        }
    }

    // ── Steps ─────────────────────────────────────────────────────────────────

    private void validateStep(CreateWorkflowRequest.StepRequest step) {
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
    }
}
