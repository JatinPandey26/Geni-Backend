package com.geni.backend.trigger.impl.github.triggers;

import com.geni.backend.Connector.impl.github.payload.GithubWebhookPayload;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.FieldType;
import com.geni.backend.common.NodeConfig;
import com.geni.backend.common.Schema;
import com.geni.backend.common.SchemaExtractor;
import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.workflow.core.ConditionDefinition;
import com.geni.backend.workflow.core.ConditionEvaluator;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubIssueUpdatedTriggerHandler extends TriggerHandler<GithubWebhookPayload> {

    private final ConditionEvaluator conditionEvaluator;

    @Override
    public TriggerDefinition buildDefinition() {

        Schema payloadSchema = SchemaExtractor.extract(GithubWebhookPayload.class);

        return TriggerDefinition.builder()
                .type(type())
                .displayName("Issue Updated")
                .source("EXTERNAL")
                .requiresIntegration(true)
                .connectorType(com.geni.backend.Connector.ConnectorType.GITHUB)
                .configSchema(Map.of(
                        "repo", FieldSchema.string("Repository name"),
                        "label", FieldSchema.builder()
                                        .type(FieldType.ARRAY)
                                        .description("Filter by issue label. Workflow will only trigger if the updated issue has this label. e.g. bug, enhancement")
                                        .required(false)
                                        .allowedOperators(List.of(
                                                ConditionDefinition.StructuredCondition.Operator.ANY_MATCH,
                                                ConditionDefinition.StructuredCondition.Operator.ALL_MATCH
                                        ))
                                .build()
                ))
                .payloadSchema(payloadSchema.getFields())
                .payloadSchemaClazz(GithubWebhookPayload.class)
                .build();
    }

    @Override
    protected TriggerType type() {
        return TriggerType.GITHUB_ISSUE_UPDATED;
    }

    @Override
    public List<WorkflowTriggerView> filter(List<WorkflowTriggerView> workflows, TriggerEvent<?> event) {
        GithubWebhookPayload payload = (GithubWebhookPayload) event.getPayload();

        return workflows.stream()
                .filter(wf -> {
                    Object repoFilter = wf.getTriggerConfig().get("repo").getValue();
                    if (repoFilter != null && !repoFilter.toString().equals(payload.getRepository().getName())) {
                        return false;
                    }

                    NodeConfig labelConfig = wf.getTriggerConfig().get("label");
                    if (labelConfig != null && labelConfig.isRequired()) {
                        var payloadLabels = payload.getIssue().getLabels();
                        var configLabels = labelConfig.getValue();

                        return conditionEvaluator.evaluateSimpleCondition(payloadLabels,labelConfig.getOperator(),configLabels);
                    }

                    return true;
                })
                .toList();
    }
}
