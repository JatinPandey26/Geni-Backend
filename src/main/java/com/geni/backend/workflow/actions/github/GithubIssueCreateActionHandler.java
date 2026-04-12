package com.geni.backend.workflow.actions.github;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.client.ConnectorClientRegistry;
import com.geni.backend.Connector.impl.github.client.GithubConnectorClient;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.geni.backend.workflow.core.ActionType.GITHUB_CREATE_ISSUE;
import static com.geni.backend.workflow.core.ConditionDefinition.StructuredCondition.Operator.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class GithubIssueCreateActionHandler extends ActionHandler {

    private final ConnectorClientRegistry connectorClientRegistry;

    @Override
    public ActionDefinition buildDefinition() {
        return ActionDefinition.builder()
                .type(type())
                .displayName("Create Issue")
                .description("Creates a new issue in a GitHub repository.")
                .connectorType(ConnectorType.GITHUB)
                .requiresIntegration(true)
                .inputSchema(Map.of(
                        "owner", FieldSchema.optionalString("Repository owner (org/user) , If none selected then integration Owner will be used"),
                        "repo", FieldSchema.string("Repository name"),
                        "title", FieldSchema.string("Issue title"),
                        "body", FieldSchema.string("Issue description"),
                        "labels", FieldSchema.builder().type(FieldType.ARRAY).collectionType(FieldType.STRING).required(false).description("Issue Labels").build() ,
                        "assignees",  FieldSchema.builder().type(FieldType.ARRAY).collectionType(FieldType.STRING).required(false).description("Assignees").build()
                ))
                .outputSchema(Map.of(
                        "issueId", FieldSchema.string("ID of the created issue"),
                        "number", FieldSchema.string("Issue number"),
                        "url", FieldSchema.string("URL of the issue"),
                        "createdAt", FieldSchema.string("Creation timestamp")
                ))
                .build();
    }

    @Override
    protected ActionType type() {
        return GITHUB_CREATE_ISSUE;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs,
                                       ExecutionContext context,
                                       Integration integration) {

        validateInputs(inputs);

        GithubConnectorClient client =
                (GithubConnectorClient) connectorClientRegistry.find(
                        ConnectorType.valueOf(integration.getConnectorType())
                );

        String owner = (String) inputs.get("owner");
        String repo = (String) inputs.get("repo");
        String title = (String) inputs.get("title");
        String body = (String) inputs.get("body");

        if(owner == null){
            owner = (String) integration.getMetadata().get("owner");
        }

        Set<String> labels = (Set<String>) inputs.getOrDefault("labels", Set.of());
        Set<String>  assignees = (Set<String>)  inputs.getOrDefault("assignees", Set.of());

        Map<String,Object> payload = Map.of("title",title,
                "labels",labels,
                "body",body,
                "assignees",assignees);

        Map<String, Object> response = client.createIssue(
                integration,
                owner,
                repo,
                payload
        );

        log.debug("GitHub issue created successfully: {}", response);

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

            if ("array".equals(fieldSchema.getType()) && !(value instanceof List)) {
                throw new IllegalArgumentException("Field '" + field + "' must be an array");
            }
        }
    }
}