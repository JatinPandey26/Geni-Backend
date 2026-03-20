package com.geni.backend.Connector.handler;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.integration.InstallCallbackResult;

import java.util.Map;

// ConnectorHandler.java
// One interface — replaces ConnectorAuthHandler + InstallFlowHandler
public interface ConnectorHandler {

    ConnectorType connectorType();

    // Called when user initiates connection
    // Slack  → stores credentials, returns Completed
    // GitHub → builds redirect URL, returns RedirectRequired
    // Jira   → builds OAuth URL, returns RedirectRequired
    InstallResult install(Map<String, Object> body, String stateToken);

    // Called only for redirect-based connectors when provider sends user back
    // Slack never hits this
    InstallCallbackResult handleCallback(Map<String, String> callbackParams) throws NoSuchMethodException;
    InstallCallbackResult handleCallback(Map<String, String> callbackParams,String response);
    // Called at execution time
    //TODO: impl integration Client
//    IntegrationClient buildClient(Integration integration);

    // Called on delete
//    void teardown(Integration integration);
}