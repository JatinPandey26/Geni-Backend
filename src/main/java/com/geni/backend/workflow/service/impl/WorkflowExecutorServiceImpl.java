package com.geni.backend.workflow.service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.trigger.service.TriggerService;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import com.geni.backend.workflow.repository.WorkflowDefinitionRepository;
import com.geni.backend.workflow.service.WorkflowExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutorServiceImpl implements WorkflowExecutorService {

    private final TriggerService triggerService;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    @Override
    public void executeWorkflow(TriggerEvent<?> triggerEvent) {
        TriggerHandler<?> triggerHandler = triggerService.getTriggerHandlersForTrigger(triggerEvent.getTriggerType().name());
        if(triggerHandler == null){
            throw new RuntimeException("No trigger handler found for event triggerType: " + triggerEvent.getTriggerType());
        }

        ConnectorType connectorType = triggerHandler.definition().getConnectorType();
        List<WorkflowTriggerView> workflowTriggerViews = workflowDefinitionRepository.findByTriggerType(triggerHandler.definition().getType().name());
        List<WorkflowTriggerView> matchedWorkflows = triggerHandler.filter(workflowTriggerViews,triggerEvent);

        // submit to executor
        log.info("Submitting workflow execution for trigger event: {} to {} workflows", triggerEvent.getTriggerType(), matchedWorkflows.size());
    }
}
