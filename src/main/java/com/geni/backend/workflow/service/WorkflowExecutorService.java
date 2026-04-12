package com.geni.backend.workflow.service;

import com.geni.backend.common.TriggerPayload;
import com.geni.backend.trigger.core.TriggerEvent;

public interface WorkflowExecutorService {
        <T extends TriggerPayload>  void executeWorkflow(TriggerEvent<T> triggerEvent);
}
