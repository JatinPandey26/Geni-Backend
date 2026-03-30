package com.geni.backend.trigger.core;

import com.geni.backend.workflow.core.WorkflowTriggerView;

import java.util.List;

public abstract class TriggerHandler<T> {

    TriggerDefinition definition;

    public abstract TriggerDefinition buildDefinition();

    protected abstract TriggerType type();

    public TriggerDefinition definition(){
        synchronized (this) {
            if (definition == null) {
                definition = buildDefinition();
            }
        }
        return definition;
    }

    public abstract List<WorkflowTriggerView> filter(
            List<WorkflowTriggerView> workflows,
            TriggerEvent<?> event
    );
}
