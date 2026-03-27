package com.geni.backend.trigger.validator;

import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.trigger.core.TriggerDefinition;

import com.geni.backend.trigger.core.TriggerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TriggerDefinitionValidator {

    private final TriggerRegistry registry;

    /**
     * Throws if the triggerDefinitionType is not registered.
     * Returns the found definition so callers can use it without a second lookup.
     */
    public TriggerDefinition validateExists(String triggerDefinitionType) {
        return registry.getByType(triggerDefinitionType);
    }

    /**
     * Validates existence and returns the definition.
     * Also checks that requiresIntegration is consistent with the supplied integrationId.
     */
    public TriggerDefinition validateWithIntegration(String triggerDefinitionType,
                                                     Long integrationId) {
        var def = validateExists(triggerDefinitionType);

        if (def.isRequiresIntegration()
                && (integrationId == null)) {
            throw new WorkflowValidationException(
                    "Trigger '" + triggerDefinitionType
                            + "' requires an integration but none was provided.");
        }

        if (!def.isRequiresIntegration() && integrationId != null) {
            throw new WorkflowValidationException(
                    "Trigger '" + triggerDefinitionType
                            + "' does not require an integration but one was provided.");
        }

        return def;
    }
}
