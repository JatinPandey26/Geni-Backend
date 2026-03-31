package com.geni.backend.workflow.validation;

import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.workflow.core.ActionDefinition;
import com.geni.backend.workflow.core.ActionHandler;
import com.geni.backend.workflow.core.ActionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionDefinitionValidator {

    private final ActionHandlerRegistry registry;

    /**
     * Throws if the actionDefinitionId is not registered.
     * Returns the found definition so callers can use it without a second lookup.
     */
    public ActionHandler validateExists(String actionDefinitionId) {
        return registry.find(actionDefinitionId)
                .orElseThrow(() -> new WorkflowValidationException(
                        "ActionDefinition not found: '" + actionDefinitionId + "'"));
    }

    /**
     * Validates existence and checks that requiresIntegration
     * is consistent with the supplied integrationId.
     */
    public ActionHandler validateWithIntegration(String stepName,
                                                    String actionDefinitionId,
                                                    Long integrationId) {
        var def = validateExists(actionDefinitionId);

        if (def.getDefinition().isRequiresIntegration()
                && (integrationId == null)) {
            throw new WorkflowValidationException(
                    "Step '" + stepName + "': action '" + actionDefinitionId
                            + "' requires an integration but none was provided.");
        }

        if (!def.definition().isRequiresIntegration() && integrationId != null) {
            throw new WorkflowValidationException(
                    "Step '" + stepName + "': action '" + actionDefinitionId
                            + "' does not require an integration but one was provided.");
        }

        return def;
    }
}
