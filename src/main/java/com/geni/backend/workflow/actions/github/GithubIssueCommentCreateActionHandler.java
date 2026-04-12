package com.geni.backend.workflow.actions.github;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.client.ConnectorClientRegistry;
import com.geni.backend.Connector.impl.github.client.GithubConnectorClient;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.integration.Integration;
import com.geni.backend.workflow.core.ActionDefinition;
import com.geni.backend.workflow.core.ActionHandler;
import com.geni.backend.workflow.core.ActionType;
import com.geni.backend.workflow.core.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.geni.backend.workflow.core.ActionType.GITHUB_ISSUE_COMMENT_CREATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubIssueCommentCreateActionHandler extends ActionHandler {

    private final ConnectorClientRegistry connectorClientRegistry;

    @Override
    public ActionDefinition buildDefinition() {
        return ActionDefinition.builder()
                .type(GITHUB_ISSUE_COMMENT_CREATE)
                .displayName("Create Issue Comment")
                .description("Adds a comment to a GitHub issue.")
                .connectorType(ConnectorType.GITHUB)
                .requiresIntegration(true)
                .inputSchema(Map.of(
                        "owner", FieldSchema.string("Repository owner (org/user)"),
                        "repo", FieldSchema.string("Repository name"),
                        "issueNumber", FieldSchema.string("Issue number"),
                        "body", FieldSchema.string("Comment text")
                ))
                .outputSchema(Map.of(
                        "commentId", FieldSchema.string("ID of the created comment"),
                        "url", FieldSchema.string("URL of the comment"),
                        "createdAt", FieldSchema.string("Creation timestamp")
                ))
                .build();
    }

    @Override
    protected ActionType type() {
        return GITHUB_ISSUE_COMMENT_CREATE;
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
        String issueNumber = (String) inputs.get("issueNumber");
        String body = (String) inputs.get("body");

        Map<String, Object> response = client.createIssueComment(
                integration,
                owner,
                repo,
                issueNumber,
                body
        );

        log.debug("GitHub comment created successfully: {}", response);

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
        }
    }
}