package com.geni.backend.workflow.core;

import com.geni.backend.common.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class TriggerResponse {
    public String              triggerDefinitionId;
    public Long              integrationId;
    public Map<String, NodeConfig> config;
}

