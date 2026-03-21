package com.geni.backend.trigger.service.impl;

import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerRegistry;
import com.geni.backend.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TriggerServiceImpl implements TriggerService {

    private final TriggerRegistry triggerRegistry;

    @Override
    public List<TriggerDefinition> getTriggerDefinitions() {
        return triggerRegistry.getAll();
    }

    @Override
    public List<TriggerDefinition> getTriggerDefinitionsForConnector(String connectorType) {
        return triggerRegistry.getByConnector(connectorType);
    }

    @Override
    public TriggerDefinition getTriggerDefinition(String triggerType) {
        return triggerRegistry.getByType(triggerType);
    }


}
