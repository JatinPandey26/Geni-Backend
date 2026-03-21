package com.geni.backend.trigger.impl.github.triggers;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.trigger.core.TriggerBaseDefinition;
import com.geni.backend.trigger.core.TriggerDefinition;
import org.springframework.stereotype.Component;

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

}
