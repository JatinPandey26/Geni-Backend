package com.geni.backend.Connector.impl.github.service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.exception.WebhookParseException;
import com.geni.backend.Connector.impl.github.GithubAppConfig;
import com.geni.backend.Connector.impl.github.GithubWebhookEvent;
import com.geni.backend.Connector.impl.github.GithubWebhookPayload;
import com.geni.backend.Connector.impl.github.service.GithubService;
import com.geni.backend.Connector.impl.github.specification.GithubIntegrationSpecification;
import com.geni.backend.integration.Service.IntegrationService;
import com.geni.backend.trigger.core.TriggerEventType;
import com.geni.backend.trigger.impl.github.triggers.GithubTriggerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GithubServiceImpl implements GithubService {

    private final GithubAppConfig githubAppConfig;
    private final ObjectMapper objectMapper;
    private final IntegrationService integrationService;
    private final GithubTriggerResolver githubTriggerResolver;

    @Override
    public void handleEvent(Map<String,String> headers, String rawBody) {

        String event = headers.get("X-GitHub-Event");
        String deliveryId  = headers.get("X-GitHub-Delivery");
        String signature   = headers.get("X-Hub-Signature-256");

        githubAppConfig.verifyWebhookSignature(rawBody,signature);

        GithubWebhookPayload payload = parse(rawBody);

        GithubWebhookEvent githubWebhookEvent = GithubWebhookEvent.from(event);

        if(githubWebhookEvent.equals(GithubWebhookEvent.INSTALLATION)){
            // if created
            // we need to save it in integration else disable it

            if(payload.getAction().equals("deleted")){
                integrationService.deleteIntegration(GithubIntegrationSpecification.hasInstallationId(payload.getInstallation().getId().toString()));
                return;
            }

            integrationService.createIntegration(ConnectorType.GITHUB.getType(),headers,rawBody);
            return;
        }

        Optional<TriggerEventType> triggerEvent = githubTriggerResolver.resolve(githubWebhookEvent, payload ,deliveryId);

        if(triggerEvent.isEmpty()){
            throw new WebhookParseException("Unsupported GitHub event type: " + event);
        }

        // trigger logic starts here
    }

    @Override
    public GithubWebhookPayload parse(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, GithubWebhookPayload.class);
        } catch (Exception e) {
            throw new WebhookParseException("Failed to parse GitHub webhook payload",e);
        }
    }
}
