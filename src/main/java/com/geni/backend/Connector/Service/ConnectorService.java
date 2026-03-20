package com.geni.backend.Connector.Service;

import com.geni.backend.Connector.ConnectorDefinition;
import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.Connector.handler.ConnectorHandler;
import com.geni.backend.common.IntegrationClient;
import com.geni.backend.integration.Integration;

import java.util.List;
import java.util.Map;

// ConnectorService.java
public interface ConnectorService {

    // definitions
    List<ConnectorDefinition> getAllDefinitions();
    ConnectorDefinition       getDefinition(ConnectorType connectorType);

    ConnectorDefinition       getDefinition(String connectorType);
    ConnectorHandler getConnectorHandler(ConnectorType connectorType);

    // install flow
//    InstallResult install(String connectorType, Map<String, Object> body);
//    Integration handleCallback(String connectorType, Map<String, String> params);

    // integration management
}