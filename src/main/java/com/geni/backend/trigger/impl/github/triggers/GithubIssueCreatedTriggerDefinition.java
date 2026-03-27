package com.geni.backend.trigger.impl.github.triggers;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.github.GithubWebhookPayload;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.trigger.core.TriggerBaseDefinition;
import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GithubIssueCreatedTriggerDefinition implements TriggerBaseDefinition {

    @Override
    public TriggerDefinition getTriggerDefinition() {
        return TriggerDefinition.builder()
                .type("GITHUB_ISSUE_CREATED")
                .displayName("Issue Created")
                .source("EXTERNAL")
                .requiresIntegration(true)
                .connectorType(ConnectorType.GITHUB)
                .configSchema(Map.of(
                        "repo",  FieldSchema.string("Repository name e.g. my-repo"),
                        "label", FieldSchema.optionalString("Filter by label e.g. bug")
                ))
                .payloadSchema(Map.of(
                        "issue.number",  FieldSchema.string("Issue number"),
                        "issue.title",   FieldSchema.string("Issue title"),
                        "issue.body",    FieldSchema.optionalString("Issue body"),
                        "issue.htmlUrl", FieldSchema.string("Link to issue"),
                        "sender.login",  FieldSchema.string("Who created it")
                ))
                .build();
    }

    @Override
    public <GithubWebhookPayload> List<WorkflowTriggerView> filter(List<WorkflowTriggerView> workflowTriggerViews, TriggerEvent<GithubWebhookPayload> triggerEvent) {
        // TODO: filter by payload and workflowTrigger configuration like repo_name and label etc
        return List.of();
    }


}
