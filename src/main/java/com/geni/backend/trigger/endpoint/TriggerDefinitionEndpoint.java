package com.geni.backend.trigger.endpoint;

import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trigger/definition")
@RequiredArgsConstructor
public class TriggerDefinitionEndpoint {

    private final TriggerService triggerService;

    @GetMapping()
    public List<TriggerDefinition> getTriggerDefinitions() {
        return triggerService.getTriggerDefinitions();
    }

    @GetMapping("/{triggerType}")
    public TriggerDefinition getTriggerDefinitionByType(@org.springframework.web.bind.annotation.PathVariable String triggerType) {
        return triggerService.getTriggerDefinition(triggerType);

    }

    @GetMapping("/connector/{connectorType}")
    public List<TriggerDefinition> getTriggerDefinitionsByConnectorType(@org.springframework.web.bind.annotation.PathVariable String connectorType) {
        return triggerService.getTriggerDefinitionsForConnector(connectorType);
    }

}
