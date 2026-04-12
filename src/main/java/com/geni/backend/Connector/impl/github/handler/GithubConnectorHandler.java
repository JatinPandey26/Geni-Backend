package com.geni.backend.Connector.impl.github.handler;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.Connector.impl.github.config.GithubAppConfig;
import com.geni.backend.Connector.impl.github.payload.GithubWebhookEvent;
import com.geni.backend.Connector.impl.github.payload.GithubWebhookPayload;
import com.geni.backend.Connector.impl.github.client.GithubConnectorClient;
import com.geni.backend.common.exception.WebhookParseException;
import com.geni.backend.Connector.handler.ConnectorHandler;
import com.geni.backend.common.IntegrationClient;
import com.geni.backend.integration.InstallCallbackResult;
import com.geni.backend.integration.Integration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Handler for GitHub App integration.
 * Manages GitHub OAuth flow, webhook callbacks, and client initialization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GithubConnectorHandler implements ConnectorHandler {

    private final GithubAppConfig githubAppConfig;
    private final GithubConnectorClient githubConnectorClient;
    private final ObjectMapper objectMapper;

    private static final String GITHUB_EVENT_HEADER = "X-GitHub-Event";
    private static final String INSTALLATION_ID_KEY = "installationId";
    private static final String OWNER_KEY = "owner";

    /**
     * Returns the connector triggerType this handler manages.
     *
     * @return ConnectorType.GITHUB
     */
    @Override
    public ConnectorType connectorType() {
        return ConnectorType.GITHUB;
    }

    /**
     * Initiates GitHub App installation by redirecting to GitHub's installation page.
     *
     * @param body the request body (unused for GitHub App flow)
     * @param stateToken the state token for OAuth security validation
     * @return RedirectRequired containing the GitHub App installation URL
     */
    @Override
    public InstallResult install(Map<String, Object> body, String stateToken) {
        log.info("Initiating GitHub App installation flow with state token");
        log.debug("Request body size: {} bytes", body != null ? body.size() : 0);

        if (stateToken == null || stateToken.trim().isEmpty()) {
            log.warn("GitHub App installation initiated without state token");
        }

        String appName = githubAppConfig.getAppName();
        if (appName == null || appName.trim().isEmpty()) {
            log.error("GitHub app name is not configured in GithubAppConfig");
            throw new IllegalStateException("GitHub app name must be configured");
        }

        String url = "https://github.com/apps/" + appName + "/installations/new?state=" + stateToken;
        log.debug("Generated GitHub App installation URL for app: {}", appName);
        log.info("Redirecting to GitHub App installation page");

        return new InstallResult.RedirectRequired(url);
    }

    /**
     * Handles OAuth callback for GitHub App installation.
     * This method is not used for GitHub App flow (uses webhook instead).
     *
     * @param params the callback parameters
     * @throws NoSuchMethodException indicating this method is not used
     */
    @Override
    public InstallCallbackResult handleCallback(Map<String, String> params) throws NoSuchMethodException {
        log.warn("Single-parameter handleCallback invoked for GitHub - this method is not used");
        throw new NoSuchMethodException(
                "GitHub App uses webhook callbacks. Use handleCallback(Map<String,String>, String) instead");
    }

    /**
     * Processes GitHub webhook callback for app installation events.
     * Extracts installation details and builds callback result.
     *
     * @param callbackParams map containing webhook headers (including X-GitHub-Event)
     * @param response the webhook payload as JSON string
     * @return InstallCallbackResult containing installation metadata
     * @throws WebhookParseException if payload cannot be parsed
     */
    @Override
    public InstallCallbackResult handleCallback(Map<String, String> callbackParams, String response) {
        log.info("Processing GitHub webhook callback");
        log.debug("Callback parameters count: {}", callbackParams != null ? callbackParams.size() : 0);

        // Validate inputs
        if (callbackParams == null || callbackParams.isEmpty()) {
            log.error("GitHub webhook callback received with empty parameters");
            throw new IllegalArgumentException("Callback parameters cannot be null or empty");
        }

        if (response == null || response.trim().isEmpty()) {
            log.error("GitHub webhook callback received with empty response body");
            throw new IllegalArgumentException("Webhook response body cannot be null or empty");
        }

        // Extract event triggerType
        String event = callbackParams.get(GITHUB_EVENT_HEADER);
        log.debug("GitHub webhook event triggerType: {}", event);

        if (event == null) {
            log.warn("GitHub webhook missing {} header", GITHUB_EVENT_HEADER);
        }

        // Parse webhook payload
        GithubWebhookPayload payload = parse(response);
        log.debug("Successfully parsed GitHub webhook payload");

        // Validate payload structure
        if (payload.getInstallation() == null) {
            log.error("GitHub webhook payload missing installation data");
            throw new WebhookParseException("GitHub webhook payload missing installation object");
        }

        if (payload.getInstallation().getAccount() == null) {
            log.error("GitHub webhook payload missing account data in installation");
            throw new WebhookParseException("GitHub webhook payload missing account object");
        }

        String accountLogin = payload.getInstallation().getAccount().getLogin();
        Long installationId = payload.getInstallation().getId();

        if (accountLogin == null || accountLogin.trim().isEmpty()) {
            log.error("GitHub webhook payload has empty account login");
            throw new WebhookParseException("GitHub account login cannot be empty");
        }

        if (installationId == null || installationId <= 0) {
            log.error("GitHub webhook payload has invalid installation ID: {}", installationId);
            throw new WebhookParseException("GitHub installation ID must be a positive number");
        }
        GithubWebhookPayload githubWebhookPayload = parse(response);
        // Parse webhook event
        GithubWebhookEvent githubWebhookEvent = GithubWebhookEvent.from(event,githubWebhookPayload);
        log.debug("Parsed webhook event: {}", githubWebhookEvent);

        // fetch access token

        Map<String,Object> githubAccessToken = githubConnectorClient.getAccessToken(String.valueOf(installationId));

        // Build installation callback result
        InstallCallbackResult installCallbackResult = InstallCallbackResult.builder()
                .name(accountLogin + " GitHub")
                .connectorType(ConnectorType.GITHUB.getType())
                .credentials(githubAccessToken)
                .externalId(String.valueOf(installationId))
                .metadata(Map.of(INSTALLATION_ID_KEY, installationId,
                                OWNER_KEY,githubWebhookPayload.getInstallation().getAccount().getLogin()))
                .build();

        log.info("GitHub webhook callback processed successfully - Installation ID: {}, Account: {}", 
                 installationId, accountLogin);
        log.debug("Install callback result: {}", installCallbackResult);

        return installCallbackResult;
    }

    /**
     * Builds a GitHub API client for the given integration.
     * Uses the installation ID and JWT to obtain an installation access token.
     *
     * @param integration the GitHub integration containing metadata
     * @return IntegrationClient for making authenticated GitHub API calls
     * @throws IllegalArgumentException if integration metadata is invalid
     */
    public IntegrationClient buildClient(Integration integration) {
//        String installationId = (String) integration.getMetadata().get("installationId");
//        String jwt            = githubAppConfig.buildJwt();
//        String accessToken    = githubApiClient.getInstallationToken(jwt, installationId);
//        return new GithubHttpClient(accessToken);
        return null;
    }


    public GithubWebhookPayload parse(String rawBody) {
        try {
            GithubWebhookPayload payload = objectMapper.readValue(rawBody, GithubWebhookPayload.class);
            log.debug("Successfully parsed GitHub webhook payload");
            return payload;
        } catch (Exception e) {
            log.error("Failed to parse GitHub webhook payload", e);
            throw new WebhookParseException("Failed to parse GitHub webhook payload", e);
        }
    }

    /**
     * Cleans up resources for a GitHub integration (currently a no-op).
     * Can be extended to revoke access tokens or perform other cleanup.
     *
     * @param integration the integration to clean up
     */
    public void teardown(Integration integration) {
        log.info("Tearing down GitHub integration: {}", integration != null ? integration.getId() : "unknown");
        // TODO: Implement token revocation or other cleanup if needed
        log.debug("GitHub integration teardown completed");
    }
}
