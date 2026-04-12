package com.geni.backend.workflow.actions.email;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.client.ConnectorClientRegistry;
import com.geni.backend.Connector.impl.gmail.client.GmailConnectorClient;
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

import java.util.Base64;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class GmailSendEmailHandler extends ActionHandler {

    private final ConnectorClientRegistry connectorClientRegistry;

    @Override
    public ActionDefinition buildDefinition() {

        return ActionDefinition.builder()
                .type(ActionType.GMAIL_SEND_EMAIL)
                .displayName("Send email")
                .description("Send an email from the authenticated Gmail account.")
                .connectorType(ConnectorType.GMAIL)
                .requiresIntegration(true)
                .inputSchema(Map.of(
                        "to", FieldSchema.string(
                                "Recipient email address. e.g. {{trigger.from}}"),
                        "subject", FieldSchema.string(
                                "Email subject line. e.g. Re: {{trigger.subject}}"),
                        "body", FieldSchema.string(
                                "Email body. Plain text or HTML. " +
                                        "e.g. {{steps.<id>.output.summary}}"),
                        "cc", FieldSchema.optionalString(
                                "CC recipients, comma-separated."),
                        "bcc", FieldSchema.optionalString(
                                "BCC recipients, comma-separated."),
                        "replyTo", FieldSchema.optionalString(
                                "Reply-to address. Defaults to the sender.")
                ))
                .outputSchema(Map.of(
                        "messageId", FieldSchema.string(
                                "Gmail message ID of the sent email."),
                        "threadId", FieldSchema.string(
                                "Gmail thread ID. Useful for reply chaining."),
                        "labelIds", FieldSchema.builder()
                                .type(FieldType.STRING)
                                .required(false)
                                .description("Labels applied to the sent message.")
                                .build()
                ))
                .build();
    }

    @Override
    protected ActionType type() {
        return ActionType.GMAIL_SEND_EMAIL;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs, ExecutionContext context, Integration integration) {
        validateInputs(inputs);
        GmailConnectorClient connectorClient = (GmailConnectorClient) this.connectorClientRegistry.find(ConnectorType.valueOf(integration.getConnectorType()));
        String rawEmail = buildRaw(inputs);
        Map<String, Object> resp = connectorClient.sendEmail(integration, rawEmail);

        log.debug("Email sent successfully. Response: {}", resp);

        // we may need to transform the response here to match the output schema defined in buildDefinition() and add validator for it.
        return resp;
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

            // this maybe not required it is responsibility of the user to provide correct email address in the input but we can add basic validation here if we want
//            if (field.toLowerCase().contains("email") || field.equals("to") || field.equals("cc") || field.equals("bcc")) {
//                validateEmailField(field, (String) value);
//            }
        }
    }

    private String buildRaw(Map<String, Object> inputs) {
        String to = (String) inputs.get("to");
        String subject = (String) inputs.get("subject");
        String body = (String) inputs.get("body");
        String cc = (String) inputs.get("cc");
        String bcc = (String) inputs.get("bcc");
        String replyTo = (String) inputs.get("replyTo");

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(buildRaw(to, subject, body, cc, bcc, replyTo).getBytes());
    }

    private String buildRaw(String to, String subject, String body,
                            String cc, String bcc, String replyTo) {
        StringBuilder sb = new StringBuilder();
        sb.append("To: ").append(to).append("\r\n");
        sb.append("Subject: ").append(subject).append("\r\n");
        if (cc != null && !cc.isBlank()) sb.append("Cc: ").append(cc).append("\r\n");
        if (bcc != null && !bcc.isBlank()) sb.append("Bcc: ").append(bcc).append("\r\n");
        if (replyTo != null && !replyTo.isBlank()) sb.append("Reply-To: ").append(replyTo).append("\r\n");
        sb.append("Content-Type: text/plain; charset=utf-8\r\nMIME-Version: 1.0\r\n\r\n");
        sb.append(body);
        return sb.toString();
    }

    private void validateEmailField(String field, String value) {

        String[] emails = value.split(",");

        for (String email : emails) {
            if (!email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("Invalid email in field '" + field + "': " + email);
            }
        }
    }
}
