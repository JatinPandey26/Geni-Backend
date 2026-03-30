package com.geni.backend.trigger.impl.github.triggers;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.github.GithubWebhookPayload;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class GithubIssueCreatedTriggerHandler
        extends TriggerHandler<GithubWebhookPayload> {


    @Override
    public TriggerDefinition buildDefinition() {
        return TriggerDefinition.builder()
                .type(type())
                .displayName("Issue Created")
                .source("EXTERNAL")
                .requiresIntegration(true)
                .connectorType(ConnectorType.GITHUB)
                .configSchema(Map.of(
                        "repo", FieldSchema.string("Repository name"),
                        "label", FieldSchema.optionalString("Label")
                ))
                .payloadSchema(Map.of(
                        "issue.title", FieldSchema.string("Title")
                ))
                .build();
    }

    @Override
    public TriggerType type() {
        return TriggerType.GITHUB_ISSUE_OPENED;
    }

    @Override
    public List<WorkflowTriggerView> filter(
            List<WorkflowTriggerView> workflows,
            TriggerEvent<?> event
    ) {

        GithubWebhookPayload payload = (GithubWebhookPayload) event.getPayload();

        return workflows.stream()
                .filter(wf -> {
                    Object repoFilter = wf.getTriggerConfig().get("repo").toString();
                    if (repoFilter != null && !repoFilter.toString().equals(payload.getRepository().getName())) {
                        return false;
                    }

                    Object labelFilter = wf.getTriggerConfig().get("label");
                    if (labelFilter != null) {
                        boolean hasLabel = payload.getIssue().getLabels().stream()
                                .anyMatch(label -> label.getName().equals(labelFilter.toString()));
                        if (!hasLabel) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();
    }
}