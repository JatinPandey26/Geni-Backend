package com.geni.backend.Connector;

import com.geni.backend.Connector.validation.ConnectorDefinitionValidator;
import com.geni.backend.common.exception.UnknownConnectorException;
import com.geni.backend.Connector.handler.ConnectorHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConnectorRegistry {

    Map<ConnectorType,ConnectorDefinition> connectorDefinitionsMap;
    Map<ConnectorType,ConnectorHandler> connectorHandlersMap;
    ConnectorDefinitionValidator connectorDefinitionValidator;


    public ConnectorRegistry(List<ConnectorDefinition> connectorDefinitions, List<ConnectorHandler> connectorHandlers) {
        connectorDefinitionValidator.validateDefinition(connectorDefinitions);
        connectorDefinitionsMap = connectorDefinitions.stream().collect(Collectors.toUnmodifiableMap(ConnectorDefinition::getType, Function.identity()));
        connectorHandlersMap = connectorHandlers.stream().collect(Collectors.toUnmodifiableMap(ConnectorHandler::connectorType,Function.identity()));
        validate();
    }

    public ConnectorDefinition getDefinition(String connectorType) {
        return Optional.ofNullable(connectorDefinitionsMap.get(ConnectorType.valueOf(connectorType)))
                .orElseThrow(() -> new UnknownConnectorException(connectorType));
    }

    public List<ConnectorDefinition> allDefinitions() {
        return List.copyOf(connectorDefinitionsMap.values());
    }

    public boolean hasDefinition(String connectorType) {
        return connectorDefinitionsMap.containsKey(connectorType);
    }

    // ── Handlers ──────────────────────────────────────────────────────

    public ConnectorHandler getHandler(String connectorType) {
        return Optional.ofNullable(connectorHandlersMap.get(ConnectorType.valueOf(connectorType)))
                .orElseThrow(() -> new UnknownConnectorException(connectorType));
    }

    public boolean hasHandler(String connectorType) {
        return connectorHandlersMap.containsKey(connectorType);
    }

    private void validate() {
        // every definition must have a matching handler
        connectorDefinitionsMap.keySet().forEach(type -> {
            if (!connectorHandlersMap.containsKey(type)) {
                throw new IllegalStateException(
                        "ConnectorDefinition registered for '" + type
                                + "' but no ConnectorHandler found"
                );
            }
        });

        // every handler must have a matching definition
        connectorHandlersMap.keySet().forEach(type -> {
            if (!connectorDefinitionsMap.containsKey(type)) {
                throw new IllegalStateException(
                        "ConnectorHandler registered for '" + type
                                + "' but no ConnectorDefinition found"
                );
            }
        });
    }




}
