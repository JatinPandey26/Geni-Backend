package com.geni.backend.trigger.impl.github.triggers;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.github.GithubWebhookPayload;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.Schema;
import com.geni.backend.common.SchemaExtractor;
import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubPushTriggerHandler
        extends TriggerHandler<GithubWebhookPayload> {

    @Override
    public TriggerDefinition buildDefinition() {

        Schema payloadSchema = SchemaExtractor.extract(GithubWebhookPayload.class);

        return TriggerDefinition.builder()
                .type(type())
                .displayName("Code Pushed to Branch")
                .source("EXTERNAL")
                .requiresIntegration(true)
                .connectorType(ConnectorType.GITHUB)
                .configSchema(Map.of(
                        "repo", FieldSchema.string("Repository name"),
                        "branch", FieldSchema.optionalString("Filter by branch name. Optional.")
                ))
                .payloadSchema(payloadSchema.getFields())
                .payloadSchemaClazz(payloadSchema.getSourceClass())
                .build();
    }

    @Override
    public TriggerType type() {
        return TriggerType.GITHUB_PUSH;
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

                    Object branchFilter = wf.getTriggerConfig().get("branch").getValue();
                    if (branchFilter != null && payload.getRef() != null) {
                        String branch = payload.getRef().replace("refs/heads/", "");
                        if (!branchFilter.toString().equals(branch)) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();
    }
}
