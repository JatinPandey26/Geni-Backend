package com.geni.backend.workflow.core;


import com.geni.backend.trigger.core.TriggerType;

import java.util.Map;

// light workflow view with trigger
public interface WorkflowTriggerView {

    String getWorkflowId();

    String getWorkflowName();

    TriggerType getTriggerType();

    Map<String, Object> getTriggerConfig();
}