package com.geni.backend.Connector.exception;

public class UnknownConnectorException extends RuntimeException {
    public UnknownConnectorException(String connectorType) {
        super("Unknown connector type: '" + connectorType + "'");
    }}
