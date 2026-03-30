package com.geni.backend.trigger.service;

import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerHandler;

import java.util.List;

public interface TriggerService {
    List<TriggerDefinition> getTriggerDefinitions();
    List<TriggerDefinition> getTriggerDefinitionsForConnector(String connectorType);
    TriggerDefinition getTriggerDefinition(String triggerType);

    TriggerHandler<?> getTriggerHandlersForTrigger(String triggerType);
    List<TriggerHandler<?>> getTriggerHandlersForConnector(String connectorType);
}
