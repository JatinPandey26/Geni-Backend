package com.geni.backend.workflow.service;

import com.geni.backend.trigger.core.TriggerEvent;

public interface WorkflowExecutorService {
        void executeWorkflow(TriggerEvent<?> triggerEvent);
}
