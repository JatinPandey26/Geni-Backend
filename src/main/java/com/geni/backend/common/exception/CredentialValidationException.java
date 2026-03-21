package com.geni.backend.common.exception;

import java.util.List;

// CredentialValidationException.java
public class CredentialValidationException extends RuntimeException {

    private final String       connectorType;
    private final List<String> errors;

    public CredentialValidationException(String connectorType, List<String> errors) {
        super("Credential validation failed for '" + connectorType + "': " + errors);
        this.connectorType = connectorType;
        this.errors        = errors;
    }

    public List<String> getErrors()      { return errors; }
    public String getConnectorType()     { return connectorType; }
}