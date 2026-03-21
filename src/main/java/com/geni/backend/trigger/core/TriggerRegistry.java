package com.geni.backend.trigger.core;

import com.geni.backend.common.exception.UnknownTriggerException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// TriggerRegistry.java
@Component
public class TriggerRegistry {

    private final Map<String, TriggerDefinition> byType;
    private final Map<String, List<TriggerDefinition>> byConnector;

    // Spring injects ALL TriggerDefinition beans automatically
    public TriggerRegistry(List<TriggerBaseDefinition> triggers) {
        List<TriggerDefinition> definitions = triggers.stream()
                .map(TriggerBaseDefinition::getTriggerDefinition)
                .toList();
        Map<String, TriggerDefinition> typeMap = new HashMap<>();

        definitions.forEach(trigger -> {
            if (typeMap.containsKey(trigger.getType())) {
                throw new IllegalStateException(
                        "Duplicate trigger type: '" + trigger.getType() + "'"
                );
            }
            typeMap.put(trigger.getType(), trigger);
        });

        this.byType      = Collections.unmodifiableMap(typeMap);
        this.byConnector = buildConnectorMap(definitions);

        validate();
    }

    public TriggerDefinition getByType(String triggerType) {
        return Optional.ofNullable(byType.get(triggerType))
                .orElseThrow(() -> new UnknownTriggerException(triggerType));
    }

    public List<TriggerDefinition> getByConnector(String connectorType) {
        return byConnector.getOrDefault(connectorType, List.of());
    }

    public List<TriggerDefinition> getAll() {
        return List.copyOf(byType.values());
    }

    public List<TriggerDefinition> getBySource(String source) {
        return byType.values().stream()
                .filter(t -> t.getSource().equals(source))
                .toList();
    }

    public boolean exists(String triggerType) {
        return byType.containsKey(triggerType);
    }

    private Map<String, List<TriggerDefinition>> buildConnectorMap(
            List<TriggerDefinition> triggers
    ) {
        // derive connector type from trigger type prefix
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
        if (byType.isEmpty()) {
            throw new IllegalStateException("No TriggerDefinitions found");
        }
    }
}