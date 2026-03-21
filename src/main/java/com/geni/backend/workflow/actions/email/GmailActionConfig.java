package com.geni.backend.workflow.actions.email;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.FieldSchema;
import com.geni.backend.workflow.core.ActionDefinition;
import com.geni.backend.workflow.core.ActionDefinitionRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GmailActionConfig {

    private final ActionDefinitionRegistry registry;

    public GmailActionConfig(ActionDefinitionRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void register() {

        // ── GMAIL_SEND_EMAIL ──────────────────────────────────────────────
        registry.register(ActionDefinition.builder()
                .type("GMAIL_SEND_EMAIL")
                .displayName("Send email")
                .description("Send an email from the authenticated Gmail account.")
                .connectorType(ConnectorType.GMAIL)
                .requiresIntegration(true)
                .inputSchema(Map.of(
                        "to",      FieldSchema.string(
                                "Recipient email address. e.g. {{trigger.from}}"),
                        "subject", FieldSchema.string(
                                "Email subject line. e.g. Re: {{trigger.subject}}"),
                        "body",    FieldSchema.string(
                                "Email body. Plain text or HTML. " +
                                        "e.g. {{steps.<id>.output.summary}}"),
                        "cc",      FieldSchema.optionalString(
                                "CC recipients, comma-separated."),
                        "bcc",     FieldSchema.optionalString(
                                "BCC recipients, comma-separated."),
                        "replyTo", FieldSchema.optionalString(
                                "Reply-to address. Defaults to the sender.")
                ))
                .outputSchema(Map.of(
                        "messageId", FieldSchema.string(
                                "Gmail message ID of the sent email."),
                        "threadId",  FieldSchema.string(
                                "Gmail thread ID. Useful for reply chaining."),
                        "labelIds",  FieldSchema.builder()
                                .type("string")
                                .required(false)
                                .description("Labels applied to the sent message.")
                                .build()
                ))
                .build());

        // ── GMAIL_REPLY_TO_EMAIL ──────────────────────────────────────────
        // Keeps the reply in the same thread — useful when trigger is
        // GMAIL_NEW_EMAIL and you want to reply to the same conversation.
        registry.register(ActionDefinition.builder()
                .type("GMAIL_REPLY_TO_EMAIL")
                .displayName("Reply to email")
                .description("Reply to an existing email thread in Gmail.")
                .connectorType(ConnectorType.GMAIL)
                .requiresIntegration(true)
                .inputSchema(Map.of(
                        "threadId", FieldSchema.string(
                                "Thread to reply to. e.g. {{trigger.threadId}}"),
                        "body",     FieldSchema.string(
                                "Reply body. Plain text or HTML."),
                        "cc",       FieldSchema.optionalString("CC recipients, comma-separated.")
                ))
                .outputSchema(Map.of(
                        "messageId", FieldSchema.string("Gmail message ID of the reply."),
                        "threadId",  FieldSchema.string("Thread ID — same as the original.")
                ))
                .build());
    }
}