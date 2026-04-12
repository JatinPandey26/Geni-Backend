package com.geni.backend.workflow.core;


import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
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
public class ActionHandlerRegistry {

    private final Map<String, ActionHandler> registry;

    public ActionHandlerRegistry(List<ActionHandler> handlers) {
        this.registry = new ConcurrentHashMap<>();
         handlers.forEach(h -> {
             if (registry.containsKey(h.definition().getType())) {
                 throw new IllegalStateException("Duplicate ActionDefinition type: " + h.definition().getType());
             }
             registry.put(String.valueOf(h.definition().getType()), h);
         });
    }

    /**
     * Called at startup by each connector's @Configuration class.
     * e.g. SlackActionConfig, GithubActionConfig, CoreActionConfig
     */
    public void register(ActionHandler handler) {
        registry.put(String.valueOf(handler.type()), handler);
    }

    public Optional<ActionHandler> find(String type) {
        return Optional.ofNullable(registry.get(type));
    }

    public ActionHandler findOrThrow(String type) {
        return find(type).orElseThrow(() ->
                new IllegalArgumentException("Unknown ActionDefinition triggerType: " + type));
    }

    public Collection<ActionHandler> all() {
        return registry.values();
    }

    /** All actions available for a given connector triggerType. */
    public Collection<ActionHandler> forConnector(
            com.geni.backend.Connector.ConnectorType connectorType) {
        return registry.values().stream()
                .filter(a -> a.definition().getConnectorType() == connectorType)
                .toList();
    }
}