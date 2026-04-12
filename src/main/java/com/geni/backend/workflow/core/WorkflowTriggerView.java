package com.geni.backend.workflow.core;


import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.NodeConfig;
import com.geni.backend.trigger.core.TriggerType;

import java.util.Map;
import java.util.UUID;

// light workflow view with trigger
public interface WorkflowTriggerView {

    UUID getWorkflowId();

    String getWorkflowName();

    TriggerType getTriggerType();

    Map<String, NodeConfig> getTriggerConfig();
}