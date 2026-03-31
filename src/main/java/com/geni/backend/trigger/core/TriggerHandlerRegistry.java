package com.geni.backend.trigger.core;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.exception.UnknownTriggerException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// TriggerHandlerRegistry.java
@Component
public class TriggerHandlerRegistry {

    private final Map<TriggerType, TriggerHandler<?>> handlerByTriggerType;
    private final Map<ConnectorType, List<TriggerHandler<?>>> handlersByConnectorType;
    // Spring injects ALL TriggerDefinition beans automatically
    public TriggerHandlerRegistry(List<TriggerHandler<?>> triggerHandlers) {
        this.handlerByTriggerType = triggerHandlers.stream()
                .collect(Collectors.toMap(
                        TriggerHandler::type,
                        h -> h
                ));

        this.handlersByConnectorType = triggerHandlers.stream()
                .collect(Collectors.groupingBy(
                        h -> h.definition().getConnectorType(),
                        Collectors.mapping(h -> h, Collectors.toList())
                ));

        validate();
    }

    public TriggerHandler<?> getByTriggerType(String triggerType) {
        return Optional.ofNullable(handlerByTriggerType.get(TriggerType.valueOf(triggerType)))
                .orElseThrow(() -> new UnknownTriggerException(triggerType));
    }

    public List<TriggerHandler<?>> getByConnector(String connectorType) {
        return handlersByConnectorType.getOrDefault(ConnectorType.valueOf(connectorType), List.of());
    }

    public List<TriggerHandler> getAll() {
        return List.copyOf(handlerByTriggerType.values());
    }

    public boolean exists(String triggerType) {
        return handlerByTriggerType.containsKey(triggerType);
    }

    private Map<String, List<TriggerDefinition>> buildConnectorMap(
            List<TriggerDefinition> triggers
    ) {
        // derive connector triggerType from trigger triggerType prefix
        // "GITHUB_ISSUE_CREATED" → "GITHUB"
        // "SLACK_MESSAGE_RECEIVED" → "SLACK"
        // "MANUAL" → "COMMON"
        // "CRON"   → "COMMON"
        return triggers.stream()
                .collect(Collectors.groupingBy(t -> t.getConnectorType().getType()));
    }

    private String extractConnectorType(String triggerType) {
        // MANUAL and CRON are not connector-specific
        if ("MANUAL".equals(triggerType) || "CRON".equals(triggerType)) {
            return "COMMON";
        }
        // "GITHUB_ISSUE_CREATED" → "GITHUB"
        return triggerType.contains("_")
                ? triggerType.split("_")[0]
                : triggerType;
    }

    private void validate() {
        // TODO : add more validation rules (e.g. required fields, naming conventions, etc.)
        // this is not a good validation
        if (handlerByTriggerType.isEmpty()) {
            throw new IllegalStateException("No TriggerDefinitions found");
        }
    }
}