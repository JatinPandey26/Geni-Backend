package com.geni.backend.trigger.validator;

import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.FieldType;
import com.geni.backend.common.NodeConfig;
import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.trigger.core.TriggerDefinition;

import com.geni.backend.trigger.core.TriggerHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TriggerDefinitionValidator {

    private final TriggerHandlerRegistry registry;

    /**
     * Throws if the triggerDefinitionType is not registered.
     * Returns the found definition so callers can use it without a second lookup.
     */
    public TriggerDefinition validateExists(String triggerDefinitionType) {
        return registry.getByTriggerType(triggerDefinitionType).definition();
    }

    /**
     * Validates existence and returns the definition.
     * Also checks that requiresIntegration is consistent with the supplied integrationId.
     */
    public TriggerDefinition validateWithIntegration(String triggerDefinitionType,
                                                     Long integrationId, Map<String,NodeConfig> config) {
        var def = validateExists(triggerDefinitionType);

        // validate configuration vs definition contract
        validateConfiguration(def, config);

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

    private void validateConfiguration(TriggerDefinition def, Map<String,NodeConfig> config) {
        def.getConfigSchema().forEach((key, schema) -> {
            if (!config.containsKey(key)) {
                throw new WorkflowValidationException(
                        "Missing required config key '" + key + "' for trigger '" + def.getDisplayName() + "'.");
            }

            FieldSchema definitionSchema = def.getConfigSchema().get(key);
            NodeConfig providedConfig = config.get(key);

            if(definitionSchema.isRequired() && providedConfig.getValue() == null) {
                throw new WorkflowValidationException(
                        "Missing required config value for key '" + key + "' for trigger '" + def.getDisplayName() + "'.");
            }

            if(providedConfig.getType() != definitionSchema.getType()) {
                throw new WorkflowValidationException(
                        "Config key '" + key + "' for trigger '" + def.getDisplayName() + "' has invalid type. Expected: " + definitionSchema.getType() + ", Provided: " + providedConfig.getType());
            }

            if(providedConfig.getValue() != null) {

                // basic type check - we can enhance this with more specific validation logic if needed (e.g. regex for string, range for number, etc.)
                switch (definitionSchema.getType()) {
                    case STRING:
                        if (!(providedConfig.getValue() instanceof String)) {
                            throw new WorkflowValidationException(
                                    "Config key '" + key + "' for trigger '" + def.getDisplayName() + "' must be a string.");
                        }
                        break;
                    case NUMBER:
                        if (!(providedConfig.getValue() instanceof Number)) {
                            throw new WorkflowValidationException(
                                    "Config key '" + key + "' for trigger '" + def.getDisplayName() + "' must be a number.");
                        }
                        break;
                    case BOOLEAN:
                        if (!(providedConfig.getValue() instanceof Boolean)) {
                            throw new WorkflowValidationException(
                                    "Config key '" + key + "' for trigger '" + def.getDisplayName() + "' must be a boolean.");
                        }
                        break;

                    case ARRAY:
                        if (!(providedConfig.getValue() instanceof Iterable)) {
                            throw new WorkflowValidationException(
                                    "Config key '" + key + "' for trigger '" + def.getDisplayName() + "' must be an array.");
                        }
                        break;
                    // add more types as needed
                    default:
                        // if the definition has an unknown type, we can choose to either ignore it or throw an error. Here we choose to ignore it.
                        break;
                }

            }

            if(providedConfig.getOperator() != null && definitionSchema.getAllowedOperators() != null
                    && !definitionSchema.getAllowedOperators().contains(providedConfig.getOperator())) {
                throw new WorkflowValidationException(
                        "Config key '" + key + "' for trigger '" + def.getDisplayName() + "' has invalid operator '" + providedConfig.getOperator() + "'. Allowed operators are: " + definitionSchema.getAllowedOperators());
            }
        });
    }
}
