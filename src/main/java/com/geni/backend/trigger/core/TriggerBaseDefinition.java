package com.geni.backend.trigger.core;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.workflow.core.WorkflowTriggerView;

import java.util.List;

public interface TriggerBaseDefinition {
    TriggerDefinition getTriggerDefinition();
    <T> List<WorkflowTriggerView> filter(List<WorkflowTriggerView> workflowTriggerViews,TriggerEvent<T> triggerEvent);
}
