package com.geni.backend.Connector.Service.impl;

import com.geni.backend.Connector.ConnectorDefinition;
import com.geni.backend.Connector.ConnectorRegistry;
import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.Service.ConnectorService;
import com.geni.backend.Connector.handler.ConnectorHandler;
import com.geni.backend.integration.repository.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnectorServiceImpl implements ConnectorService {

    private final ConnectorRegistry connectorRegistry;
    private final IntegrationRepository integrationRepository;

    // ── Definitions ───────────────────────────────────────────────────

    @Override
    public List<ConnectorDefinition> getAllDefinitions() {
        return connectorRegistry.allDefinitions();
    }

    @Override
    public ConnectorDefinition getDefinition(ConnectorType connectorType) {
        return connectorRegistry.getDefinition(connectorType.getType());   // throws if unknown
    }

    @Override
    public ConnectorDefinition getDefinition(String connectorType) {
        return connectorRegistry.getDefinition(connectorType);   // throws if unknown
    }

    @Override
    public ConnectorHandler getConnectorHandler(ConnectorType connectorType){
       return connectorRegistry.getHandler(connectorType.getType());
    }

    private Map<String, String> toStringMap(Map<String, Object> body) {
        return body.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toString()
                ));
    }
}