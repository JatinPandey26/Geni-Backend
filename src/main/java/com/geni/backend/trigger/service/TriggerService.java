package com.geni.backend.trigger.service;

import com.geni.backend.trigger.core.TriggerBaseDefinition;
import com.geni.backend.trigger.core.TriggerDefinition;

import java.util.List;

public interface TriggerService {
    List<TriggerDefinition> getTriggerDefinitions();
    List<TriggerDefinition> getTriggerDefinitionsForConnector(String connectorType);
    TriggerDefinition getTriggerDefinition(String triggerType);
}
