package com.geni.backend.trigger.impl.github.triggers;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.github.payload.GithubWebhookPayload;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.Schema;
import com.geni.backend.common.SchemaExtractor;
import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.workflow.core.ConditionEvaluator;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubIssueAssignedTriggerHandler
        extends TriggerHandler<GithubWebhookPayload> {

    private final ConditionEvaluator conditionEvaluator;

    @Override
    public TriggerDefinition buildDefinition() {

        Schema payloadSchema = SchemaExtractor.extract(GithubWebhookPayload.class);

        return TriggerDefinition.builder()
                .type(type())
                .displayName("Issue Assigned")
                .source("EXTERNAL")
                .requiresIntegration(true)
                .connectorType(ConnectorType.GITHUB)
                .configSchema(Map.of(
                        "repo", FieldSchema.string("Repository name"),
                        "assignee", FieldSchema.string("Filter by assignee login. Optional.")
                ))
                .payloadSchema(payloadSchema.getFields())
                .payloadSchemaClazz(payloadSchema.getSourceClass())
                .build();
    }

    @Override
    public TriggerType type() {
        return TriggerType.GITHUB_ISSUE_ASSIGNED;
    }

    @Override
    public List<WorkflowTriggerView> filter(
            List<WorkflowTriggerView> workflows,
            TriggerEvent<?> event
    ) {

        GithubWebhookPayload payload = (GithubWebhookPayload) event.getPayload();

        return workflows.stream()
                .filter(wf -> {
                    Object repoFilter = wf.getTriggerConfig().get("repo").getValue();
                    if (repoFilter != null && !repoFilter.toString().equals(payload.getRepository().getName())) {
                        return false;
                    }

                    Object assigneeFilter = wf.getTriggerConfig().get("assignee").getValue();
                    if (assigneeFilter != null && payload.getIssue() != null && payload.getIssue().getAssignee() != null) {
                        if (!assigneeFilter.toString().equals(payload.getIssue().getAssignee().getLogin())) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();
    }
}
