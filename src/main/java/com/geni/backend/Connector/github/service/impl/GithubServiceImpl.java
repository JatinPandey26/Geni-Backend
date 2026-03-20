package com.geni.backend.Connector.github.service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.exception.WebhookParseException;
import com.geni.backend.Connector.github.GithubAppConfig;
import com.geni.backend.Connector.github.GithubWebhook;
import com.geni.backend.Connector.github.GithubWebhookEvent;
import com.geni.backend.Connector.github.GithubWebhookPayload;
import com.geni.backend.Connector.github.service.GithubService;
import com.geni.backend.Connector.github.specification.GithubIntegrationSpecification;
import com.geni.backend.integration.InstallCallbackResult;
import com.geni.backend.integration.Service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubServiceImpl implements GithubService {

    private final GithubAppConfig githubAppConfig;
    private final ObjectMapper objectMapper;
    private final IntegrationService integrationService;

    @Override
    public void handleEvent(Map<String,String> headers, String rawBody) {

        String event = headers.get("X-GitHub-Event");
        String deliveryId  = headers.get("X-GitHub-Delivery");
        String signature   = headers.get("X-Hub-Signature-256");

        githubAppConfig.verifyWebhookSignature(rawBody,signature);

        GithubWebhookPayload payload = parse(rawBody);

        GithubWebhookEvent githubWebhookEvent = GithubWebhookEvent.from(event);

        // trigger logic starts here

        if(githubWebhookEvent.equals(GithubWebhookEvent.INSTALLATION)){
            // if created
            // we need to save it in integration else disable it

            if(payload.getAction().equals("deleted")){
                integrationService.deleteIntegration(GithubIntegrationSpecification.hasInstallationId(payload.getInstallation().getId().toString()));
                return;
            }

            integrationService.createIntegration(ConnectorType.GITHUB.getType(),headers,rawBody);
        }
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
