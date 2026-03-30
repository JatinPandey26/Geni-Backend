package com.geni.backend.trigger.service.impl;

import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.trigger.core.TriggerHandlerRegistry;
import com.geni.backend.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TriggerServiceImpl implements TriggerService {

    private final TriggerHandlerRegistry triggerHandlerRegistry;

    @Override
    public List<TriggerDefinition> getTriggerDefinitions() {
        return triggerHandlerRegistry.getAll().stream().map(TriggerHandler::definition).toList();
    }

    @Override
    public List<TriggerDefinition> getTriggerDefinitionsForConnector(String connectorType) {
        List<TriggerHandler<?>> handlers = triggerHandlerRegistry.getByConnector(connectorType);
        return handlers.stream()
                .map(TriggerHandler::definition)
                .toList();
    }

    @Override
    public TriggerDefinition getTriggerDefinition(String triggerType) {
        return triggerHandlerRegistry.getByTriggerType(triggerType).definition();
    }

    @Override
    public TriggerHandler<?> getTriggerHandlersForTrigger(String triggerType) {
        return triggerHandlerRegistry.getByTriggerType(triggerType);
    }

    @Override
    public List<TriggerHandler<?>> getTriggerHandlersForConnector(String connectorType) {
        return triggerHandlerRegistry.getByConnector(connectorType);
    }


}
