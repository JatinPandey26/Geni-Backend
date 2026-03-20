package com.geni.backend.Connector.validation;

import com.geni.backend.Connector.ConnectorDefinition;
import com.geni.backend.Connector.ConnectorRegistry;
import com.geni.backend.Connector.exception.CredentialValidationException;
import com.geni.backend.common.CredentialField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// ConnectorDefinitionValidator.java
@Component
@RequiredArgsConstructor
public class ConnectorDefinitionValidator {

    private final ConnectorRegistry connectorRegistry;

    // called before install — validates body has all required credential fields
    public void validateCredentials(String connectorType, Map<String, Object> body) {
        ConnectorDefinition definition = connectorRegistry.getDefinition(connectorType);
        List<CredentialField> credentialFields = definition.getCredentialFields();

        if (credentialFields.isEmpty()) return;  // redirect-based — nothing to validate

        List<String> errors = new ArrayList<>();

        credentialFields.forEach(field -> {
            Object value = body.get(field.getKey());

            // required field missing entirely
            if (field.isRequired() && value == null) {
                errors.add("Missing required field: '" + field.getKey() + "'");
                return;
            }

            // required field present but blank
            if (field.isRequired() && value.toString().isBlank()) {
                errors.add("Field '" + field.getKey() + "' cannot be blank");
                return;
            }

            // secret field must meet minimum length
            if (field.isSecret() && value != null
                    && value.toString().length() < 8) {
                errors.add("Field '" + field.getKey() + "' is too short to be valid");
            }
        });

        if (!errors.isEmpty()) {
            throw new CredentialValidationException(connectorType, errors);
        }
    }

    // validate the definition itself at registry startup
    public void validateDefinition(ConnectorDefinition definition) {
        List<String> errors = new ArrayList<>();

        if (definition.getType() == null) {
            errors.add("ConnectorDefinition must have a type");
        }

        if (definition.getDisplayName() == null || definition.getDisplayName().isBlank()) {
            errors.add("ConnectorDefinition must have a display name");
        }

        if (definition.getCredentialFields() == null) {
            errors.add("ConnectorDefinition credentialFields cannot be null — use List.of() if empty");
        }

        // check for duplicate keys in credential fields
        if (definition.getCredentialFields() != null) {
            Set<String> seen   = new HashSet<>();
            Set<String> dupes  = new HashSet<>();

            definition.getCredentialFields().forEach(field -> {
                if (field.getKey() == null || field.getKey().isBlank()) {
                    errors.add("CredentialField has null or blank key");
                } else if (!seen.add(field.getKey())) {
                    dupes.add(field.getKey());
                }
            });

            if (!dupes.isEmpty()) {
                errors.add("Duplicate credential field keys: " + dupes);
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid ConnectorDefinition [" + definition.getType() + "]: " + errors
            );
        }
    }
}
