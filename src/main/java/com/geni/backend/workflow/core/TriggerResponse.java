package com.geni.backend.workflow.core;

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
    public String              integrationId;
    public Map<String, Object> config;
}

