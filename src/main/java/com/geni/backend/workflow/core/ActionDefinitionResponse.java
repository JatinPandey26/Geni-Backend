package com.geni.backend.workflow.core;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.FieldSchema;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ActionDefinitionResponse {

    String        type;                // "GMAIL_SEND_EMAIL"
    String        displayName;         // "Send email"
    String        description;
    ConnectorType connectorType;       // GMAIL, SLACK, CORE …
    boolean       requiresIntegration;

    // what the user must fill in when configuring this step
    Map<String, FieldSchema> inputSchema;

    // what this action returns — available as {{steps.<id>.output.x}}
    // shown in the field mapper when a downstream step references this step
    Map<String, FieldSchema> outputSchema;
}
