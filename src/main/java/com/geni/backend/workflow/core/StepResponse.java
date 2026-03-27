package com.geni.backend.workflow.core;

import com.geni.backend.workflow.enums.OnErrorStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
public class StepResponse {
    UUID id;
    String name;

    UUID               parentStepId;    // null = root step
    ConditionDefinition edgeCondition;  // null = unconditional
    String              edgeLabel;

    String actionDefinitionId;
    Long integrationId;

    Map<String, String> fieldMappings;

    OnErrorStrategy onError;
    RetryConfigResponse retryConfig;    // null unless onError = RETRY
}