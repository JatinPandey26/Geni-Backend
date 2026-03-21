package com.geni.backend.workflow.core;


import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of all ActionDefinitions.
 *
 * ActionDefinitions are code — not DB rows. They are registered at startup
 * by each connector module and by the built-in CORE actions.
 *
 * Pattern mirrors however you currently store TriggerDefinitions.
 * If you have a TriggerDefinitionRegistry, this is identical.
 */
@Component
public class ActionDefinitionRegistry {

    private final Map<String, ActionDefinition> registry = new ConcurrentHashMap<>();

    /**
     * Called at startup by each connector's @Configuration class.
     * e.g. SlackActionConfig, GithubActionConfig, CoreActionConfig
     */
    public void register(ActionDefinition definition) {
        registry.put(definition.getType(), definition);
    }

    public Optional<ActionDefinition> find(String type) {
        return Optional.ofNullable(registry.get(type));
    }

    public ActionDefinition findOrThrow(String type) {
        return find(type).orElseThrow(() ->
                new IllegalArgumentException("Unknown ActionDefinition type: " + type));
    }

    public Collection<ActionDefinition> all() {
        return registry.values();
    }

    /** All actions available for a given connector type. */
    public Collection<ActionDefinition> forConnector(
            com.geni.backend.Connector.ConnectorType connectorType) {
        return registry.values().stream()
                .filter(a -> a.getConnectorType() == connectorType)
                .toList();
    }
}