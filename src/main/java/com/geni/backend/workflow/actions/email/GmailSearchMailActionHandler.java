package com.geni.backend.workflow.actions.email;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.client.ConnectorClientRegistry;
import com.geni.backend.Connector.impl.gmail.client.GmailConnectorClient;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.FieldType;
import com.geni.backend.integration.Integration;
import com.geni.backend.workflow.core.ActionDefinition;
import com.geni.backend.workflow.core.ActionHandler;
import com.geni.backend.workflow.core.ActionType;
import com.geni.backend.workflow.core.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.geni.backend.workflow.core.ActionType.GMAIL_SEARCH_EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailSearchMailActionHandler extends ActionHandler {

    private final ConnectorClientRegistry connectorClientRegistry;

    @Override
    public ActionDefinition buildDefinition() {

        return ActionDefinition.builder()
                .type(GMAIL_SEARCH_EMAIL)
                .displayName("Send email")
                .description("Send an email from the authenticated Gmail account.")
                .connectorType(ConnectorType.GMAIL)
                .requiresIntegration(true)
                .inputSchema(Map.of(
                        "query", FieldSchema.string("Gmail search query e.g. from:test@gmail.com"),
                        "maxResults", FieldSchema.optionalNumber("Max results")
                ))
                .outputSchema(Map.of(
                        "messages", FieldSchema.builder()
                                .type(FieldType.ARRAY)
                                .description("List of email messages matching the search query. Each message contains basic details like id, threadId, snippet, etc.")
                                .build(),
                        "resultSize", FieldSchema.optionalNumber(
                                "Number of emails returned")                ))
                .build();
    }

    @Override
    protected ActionType type() {
        return GMAIL_SEARCH_EMAIL;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs, ExecutionContext context, Integration integration) {
        validateInputs(inputs);
        GmailConnectorClient connectorClient = (GmailConnectorClient) this.connectorClientRegistry.find(ConnectorType.valueOf(integration.getConnectorType()));
        String query = (String) inputs.get("query");

        Integer maxResults = inputs.get("maxResults") != null
                ? Math.min(((Number) inputs.get("maxResults")).intValue(), 50) // cap
                : 10;

        Map<String, Object> response =
                connectorClient.searchEmails(integration, query, maxResults);

        log.debug("Search emails response: {}", response);

        return response;
    }

    @Override
    protected void validateInputs(Map<String, Object> inputs) {

        Map<String, FieldSchema> schema = buildDefinition().getInputSchema();

        for (Map.Entry<String, FieldSchema> entry : schema.entrySet()) {

            String field = entry.getKey();
            FieldSchema fieldSchema = entry.getValue();
            Object value = inputs.get(field);

            if (fieldSchema.isRequired() && value == null) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }

            if (value == null) continue;

            if ("string".equals(fieldSchema.getType()) && !(value instanceof String)) {
                throw new IllegalArgumentException("Field '" + field + "' must be a string");
            }

            if (field.equals("maxResults") && value != null) {
                if (!(value instanceof Number)) {
                    throw new IllegalArgumentException("Field 'maxResults' must be a number");
                }
                int maxResults = ((Number) value).intValue();
                if (maxResults <= 0) {
                    throw new IllegalArgumentException("Field 'maxResults' must be a positive integer");
                }
            }

        }
    }
}

