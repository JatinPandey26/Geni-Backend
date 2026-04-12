package com.geni.backend.Connector.impl.gmail.Service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.gmail.client.GmailConnectorClient;
import com.geni.backend.Connector.impl.gmail.payload.Attachment;
import com.geni.backend.Connector.impl.gmail.payload.GmailMessageMapper;
import com.geni.backend.Connector.impl.gmail.payload.GmailMessagePayload;
import com.geni.backend.Connector.impl.gmail.raw.GmailHistory;
import com.geni.backend.Connector.impl.gmail.raw.GmailHistoryResponse;
import com.geni.backend.Connector.impl.gmail.raw.GmailMessageAdded;
import com.geni.backend.Connector.impl.gmail.raw.GmailRawMessage;
import com.geni.backend.Connector.impl.gmail.raw.GmailPushData;
import com.geni.backend.Connector.impl.gmail.payload.GmailWebhookPayload;
import com.geni.backend.Connector.impl.gmail.Service.GmailService;
import com.geni.backend.integration.Integration;
import com.geni.backend.integration.Service.IntegrationService;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerHandler;
import com.geni.backend.trigger.core.TriggerHandlerRegistry;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.workflow.service.WorkflowExecutorService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class GmailServiceImpl implements GmailService {

    private final IntegrationService integrationService;
    private final ObjectMapper objectMapper;
    private final TriggerHandlerRegistry triggerHandlerRegistry;
    private final WorkflowExecutorService workflowExecutorService;
    private final GmailConnectorClient connectorClient;

    @Override
    public void handleRegistration(Map<String, String> payload) {
        integrationService.createIntegration(ConnectorType.GMAIL.getType(), payload);
    }

    @Override
    @Transactional
    public void handleEvent(Map<String, String> payload, String rawBody) {
        GmailPushData pushData = parsePayload(rawBody);
        log.debug("Parsed Gmail pushData payload: {}", pushData);

        Integration integration = integrationService
                .fetchByConnectorTypeAndExternalId(ConnectorType.GMAIL.getType(), pushData.getEmailAddress());

        // fetch history then decide event
        GmailHistoryResponse gmailHistoryResponse = fetchGmailEventsFromHistory(integration, pushData);
        if(gmailHistoryResponse.getHistory() != null) {
            for (GmailHistory gmailHistory : gmailHistoryResponse.getHistory()) {
                if(gmailHistory.getMessagesAdded() != null) {
                    // process messagesAdded
                    for (GmailMessageAdded messageAdded : gmailHistory.getMessagesAdded()) {
                        // For simplicity, we assume every webhook event corresponds to the same trigger type.
                        TriggerType triggerType = TriggerType.GMAIL_NEW_EMAIL;

                        TriggerHandler<?> handler = triggerHandlerRegistry.getByTriggerType(triggerType.name());

                        if (handler == null) {
                            log.error("No trigger handler found for trigger type: {}", triggerType.name());
                            return;
                        }

                        Map<String, Object> messageRawResponse = connectorClient.getMessage(integration, messageAdded.getMessage().getId());
                        GmailRawMessage gmailRawMessage = objectMapper.convertValue(messageRawResponse, GmailRawMessage.class);
                        GmailMessagePayload gmailMessagePayload = GmailMessageMapper.map(gmailRawMessage);
                        fetchAndEmbedAttachment(gmailMessagePayload,integration);
                        TriggerEvent<GmailMessagePayload> triggerEvent = TriggerEvent.<GmailMessagePayload>builder()
                                .triggerType(triggerType)
                                .payload(gmailMessagePayload)
                                .build();

                        workflowExecutorService.executeWorkflow(triggerEvent);
                    }
                }
            }
        }

        integration.getMetadata().put("historyId",gmailHistoryResponse.getHistoryId());
        integrationService.updateIntegration(integration);

    }



    private GmailHistoryResponse fetchGmailEventsFromHistory(Integration integration, GmailPushData pushData) {
        if (pushData == null || pushData.getEmailAddress() == null) {
            throw new IllegalArgumentException("Invalid Gmail push data");
        }

        if (integration == null) {
            throw new IllegalStateException(
                    "No Gmail integration found for email: " + pushData.getEmailAddress()
            );
        }

        Long prevHistoryId = Long.valueOf(integration.getMetadata().get("historyId").toString());

        Map<String, Object> historyResponse = connectorClient.listHistory(integration, prevHistoryId.toString());

        GmailHistoryResponse gmailHistoryResponse = objectMapper.convertValue(historyResponse, GmailHistoryResponse.class);

        return gmailHistoryResponse;
    }

    GmailPushData parsePayload(String rawBody) {
        // object mapper to parse rawBody into GmailWebhookPayload
        try {
            GmailWebhookPayload webhookPayload = objectMapper.readValue(rawBody, GmailWebhookPayload.class);

            String decodedJson = new String(java.util.Base64.getDecoder().decode(webhookPayload.getMessage().getData()));
            return objectMapper.readValue(decodedJson, GmailPushData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gmail webhook payload", e);
        }
    }

    private void fetchAndEmbedAttachment(GmailMessagePayload email,Integration integration) {
        if(email.getBody() == null) return;
        StringBuilder issueBodyBuilder = new StringBuilder(email.getBody());

        for (Attachment att : email.getAttachments()) {

            Map<String, Object> response = connectorClient.getAttachment(
                    integration,
                    email.getMessageId(),
                    att.getAttachmentId()
            );

            String base64Data = (String) response.get("data");

            if (att.getMimeType().startsWith("image/")) {
                issueBodyBuilder.append("\n")
                        .append(String.format("![%s](data:%s;base64,%s)",
                                att.getFileName(),
                                att.getMimeType(),
                                base64Data));
            } else {
                issueBodyBuilder.append("\n")
                        .append(String.format("[%s](data:%s;base64,%s)",
                                att.getFileName(),
                                att.getMimeType(),
                                base64Data));
            }
        }

        email.setBody(issueBodyBuilder.toString());
    }
}
