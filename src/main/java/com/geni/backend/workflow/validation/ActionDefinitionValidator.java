package com.geni.backend.workflow.validation;

import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.workflow.core.ActionDefinition;
import com.geni.backend.workflow.core.ActionDefinitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionDefinitionValidator {

    private final ActionDefinitionRegistry registry;

    /**
     * Throws if the actionDefinitionId is not registered.
     * Returns the found definition so callers can use it without a second lookup.
     */
    public ActionDefinition validateExists(String actionDefinitionId) {
        return registry.find(actionDefinitionId)
                .orElseThrow(() -> new WorkflowValidationException(
                        "ActionDefinition not found: '" + actionDefinitionId + "'"));
    }

    /**
     * Validates existence and checks that requiresIntegration
     * is consistent with the supplied integrationId.
     */
    public ActionDefinition validateWithIntegration(String stepName,
                                                    String actionDefinitionId,
                                                    Long integrationId) {
        var def = validateExists(actionDefinitionId);

        if (def.isRequiresIntegration()
                && (integrationId == null)) {
            throw new WorkflowValidationException(
                    "Step '" + stepName + "': action '" + actionDefinitionId
                            + "' requires an integration but none was provided.");
        }

        if (!def.isRequiresIntegration() && integrationId != null) {
            throw new WorkflowValidationException(
                    "Step '" + stepName + "': action '" + actionDefinitionId
                            + "' does not require an integration but one was provided.");
        }

        return def;
    }
}
