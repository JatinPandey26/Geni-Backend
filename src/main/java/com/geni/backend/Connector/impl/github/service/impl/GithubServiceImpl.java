package com.geni.backend.Connector.impl.github.service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.exception.WebhookParseException;
import com.geni.backend.Connector.impl.github.config.GithubAppConfig;
import com.geni.backend.Connector.impl.github.payload.GithubWebhookEvent;
import com.geni.backend.Connector.impl.github.payload.GithubWebhookPayload;
import com.geni.backend.Connector.impl.github.service.GithubService;
import com.geni.backend.Connector.impl.github.specification.GithubIntegrationSpecification;
import com.geni.backend.integration.Service.IntegrationService;
import com.geni.backend.trigger.core.TriggerEvent;
import com.geni.backend.trigger.core.TriggerType;
import com.geni.backend.trigger.impl.github.triggers.GithubTriggerResolver;
import com.geni.backend.workflow.service.WorkflowExecutorService;
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
    private final WorkflowExecutorService workflowExecutorService;

    @Override
    public void handleEvent(Map<String,String> headers, String rawBody) {

        String event = headers.get("X-GitHub-Event");
        String deliveryId  = headers.get("X-GitHub-Delivery");
        String signature   = headers.get("X-Hub-Signature-256");

        githubAppConfig.verifyWebhookSignature(rawBody,signature);

        GithubWebhookPayload payload = parse(rawBody);

        GithubWebhookEvent githubWebhookEvent = GithubWebhookEvent.from(event,payload);

        if(githubWebhookEvent.equals(GithubWebhookEvent.INSTALLATION)){
            // if created
            // we need to save it in integration else disable it

            if(payload.getAction().equals("deleted")){
                integrationService.deleteIntegration(GithubIntegrationSpecification.hasInstallationId(payload.getInstallation().getId().toString()));

                //TODO : we need to remove trigger integration Id from wfs those wf triggers will become orphan if user sees them in UI they will get warning of
                // reattaching trigger integration.


                return;
            }

            integrationService.createIntegration(ConnectorType.GITHUB.getType(),headers,rawBody);
            return;
        }

        Optional<TriggerType> triggerType = githubTriggerResolver.resolve(githubWebhookEvent, payload ,deliveryId);

        if(triggerType.isEmpty()){
            throw new WebhookParseException("Unsupported GitHub event triggerType: " + event);
        }

        TriggerEvent<GithubWebhookPayload> triggerEvent = TriggerEvent.<GithubWebhookPayload>builder()
                .triggerType(triggerType.get())
                .payload(payload)
                .build();

        workflowExecutorService.executeWorkflow(triggerEvent);
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
