package com.geni.backend.trigger.impl.email.triggers;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.gmail.payload.GmailMessagePayload;
import com.geni.backend.Connector.impl.gmail.raw.GmailRawMessage;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.common.FieldType;
import com.geni.backend.common.Schema;
import com.geni.backend.common.SchemaExtractor;
import com.geni.backend.trigger.core.TriggerDefinition;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.workflow.core.ConditionDefinition;
import com.geni.backend.workflow.core.WorkflowTriggerView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.geni.backend.workflow.core.ConditionDefinition.*;
import static com.geni.backend.workflow.core.ConditionDefinition.StructuredCondition.Operator.CONTAINS;
import static com.geni.backend.workflow.core.ConditionDefinition.StructuredCondition.*;
import static com.geni.backend.workflow.core.ConditionDefinition.StructuredCondition.Operator.NOT_CONTAINS;

@Component
public class GmailNewEmailTriggerHandler extends TriggerHandler<GmailRawMessage> {
    @Override
    public TriggerDefinition buildDefinition() {
        Schema payloadSchema = SchemaExtractor.extract(GmailMessagePayload.class);

        return TriggerDefinition.builder()
                .type(type())
                .displayName("New Email Received")
                .source("EXTERNAL")
                .requiresIntegration(true)
                .connectorType(ConnectorType.GMAIL)
                .configSchema(Map.of(
                        "from", FieldSchema.optionalString("Filter by sender email"),
                        "subjectContains", FieldSchema.builder()
                                        .description("Filter by subject keyword")
                                        .type(FieldType.STRING)
                                        .allowedOperators(List.of(CONTAINS,NOT_CONTAINS))
                                .build()
                ))
                .payloadSchema(payloadSchema.getFields())
                .payloadSchemaClazz(payloadSchema.getSourceClass())
                .build();
    }

    @Override
    protected TriggerType type() {
        return TriggerType.GMAIL_NEW_EMAIL;
    }

    @Override
    public List<WorkflowTriggerView> filter(
            List<WorkflowTriggerView> workflows,
            TriggerEvent<?> event
    ) {

        GmailMessagePayload payload = (GmailMessagePayload) event.getPayload();

        return workflows.stream()
                .filter(wf -> {

                    Object fromFilter = wf.getTriggerConfig().get("from") != null
                            ? wf.getTriggerConfig().get("from").getValue()
                            : null;

                    String fromInPayload = payload.getFrom();
                    if (fromFilter != null &&
                            !fromFilter.toString().equalsIgnoreCase(fromInPayload)) {
                        return false;
                    }

                    Object subjectFilter = wf.getTriggerConfig().get("subjectContains") != null
                            ? wf.getTriggerConfig().get("subjectContains").getValue()
                            : null;
                    Operator operatorForSubjectFilter = wf.getTriggerConfig().get("subjectContains").getOperator();

                    String subjectInPayload = payload.getSubject();

                    if (subjectFilter != null &&
                            (subjectInPayload == null ||
                                    (!subjectInPayload.toLowerCase()
                                            .contains(subjectFilter.toString().toLowerCase()) && operatorForSubjectFilter.equals(CONTAINS)))) {
                        return false;
                    }

                    return true;
                })
                .toList();
    }
}
