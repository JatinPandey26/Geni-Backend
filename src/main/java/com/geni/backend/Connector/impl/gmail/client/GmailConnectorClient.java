package com.geni.backend.Connector.impl.gmail.client;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.client.ConnectorClient;
import com.geni.backend.Connector.impl.gmail.config.GmailConnectorConfig;
import com.geni.backend.secret.service.SecretService;
import org.springframework.stereotype.Component;
import com.geni.backend.integration.Integration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GmailConnectorClient implements ConnectorClient {

    private static final String GMAIL_API_BASE = "https://gmail.googleapis.com/gmail/v1/users/me";
    private static final String TOKEN_URL      = "https://oauth2.googleapis.com/token";

    private final GmailConnectorConfig config;
    private final RestTemplate restTemplate;
    private final SecretService secretService;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Sends an email. Automatically refreshes token on 401.
     */
    public Map<String, Object> sendEmail(Integration integration, String rawEmailBase64) {
        return executeWithTokenRefresh(integration, accessToken ->
                post(GMAIL_API_BASE + "/messages/send",
                        accessToken,
                        Map.of("raw", rawEmailBase64))
        );
    }

    public Map<String,Object> replyToEmail(Integration integration, String messageId, String rawEmailBase64){
        return executeWithTokenRefresh(integration, accessToken ->
                post(GMAIL_API_BASE + "/messages/" + messageId + "/reply",
                        accessToken,
                        Map.of("raw", rawEmailBase64))
        );
    }

    /**
     * Fetches a single message by ID.
     */
    public Map<String, Object> getMessage(Integration integration, String messageId) {
        return executeWithTokenRefresh(integration, accessToken ->
                get(GMAIL_API_BASE + "/messages/" + messageId, accessToken)
        );
    }

    /**
     * Lists messages matching a query.
     * e.g. query = "in:inbox is:unread"
     */
    public Map<String, Object> listMessages(Integration integration, String query) {
        return executeWithTokenRefresh(integration, accessToken ->
                get(GMAIL_API_BASE + "/messages?q=" + query, accessToken)
        );
    }

    /**
     * Calls Gmail watch() to start Pub/Sub push notifications.
     * Must be called after OAuth and re-called every 7 days.
     */
    public Map<String, Object> startWatch(Integration integration, String topicName) {
        return executeWithTokenRefresh(integration, accessToken ->
                post(GMAIL_API_BASE + "/watch",
                        accessToken,
                        Map.of("topicName", topicName,
                                "labelIds",  java.util.List.of("INBOX")))
        );
    }

    /**
     * Lists history since a given historyId — used by Pub/Sub webhook handler
     * to find new messages since last notification.
     */
    public Map<String, Object> listHistory(Integration integration, String startHistoryId) {
        return executeWithTokenRefresh(integration, accessToken ->
                get(GMAIL_API_BASE + "/history?startHistoryId=" + startHistoryId
                        + "&historyTypes=messageAdded", accessToken)
        );
    }

    public Map<String,Object> searchEmails(Integration integration, String query,int maxResults){
        return executeWithTokenRefresh(integration, accessToken ->
                get(GMAIL_API_BASE + "/messages?q=" + query + "&maxResults=" + maxResults, accessToken)
        );
    }

    public Map<String, Object> getAttachment(Integration integration,
                                             String messageId,
                                             String attachmentId) {

        return executeWithTokenRefresh(integration,(accessToken ->
                get(GMAIL_API_BASE + "/messages/" + messageId + "/attachments/" + attachmentId,accessToken)));
    }

    // ── Token refresh ──────────────────────────────────────────────────────────

    /**
     * Executes an API call. On 401 → refreshes token → retries once.
     * Throws RuntimeException if refresh fails or second attempt also fails.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeWithTokenRefresh(
            Integration integration,
            ApiCall apiCall) {

        Map<String, Object> credentials = secretService.getSecret(integration.getCredentialRef(),Map.class);
        String accessToken = String.valueOf(credentials.get("access_token"));

        try {
            return apiCall.execute(accessToken);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                throw e; // not a token issue — rethrow as-is
            }

            log.info("Gmail 401 for integration {} — attempting token refresh",
                    integration.getId());

            // refresh and retry once
            String newAccessToken = refreshAccessToken(integration, credentials);
            log.info("Token refreshed for integration {} — retrying request",
                    integration.getId());

            return apiCall.execute(newAccessToken);
        }
    }

    /**
     * Refreshes the access token using the stored refresh_token.
     * Persists the new access_token back to SecretProvider.
     * Returns the new access token.
     */
    @SuppressWarnings("unchecked")
    private String refreshAccessToken(Integration integration,
                                      Map<String, Object> credentials) {
        String refreshToken = String.valueOf(credentials.get("refresh_token"));
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException(
                    "No refresh_token available for integration " + integration.getId()
                            + " — user must reconnect their Gmail account.");
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var body = new LinkedMultiValueMap<String, String>();
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("refresh_token", refreshToken);
        body.add("grant_type",    "refresh_token");

        try {
            var response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException(
                        "Token refresh failed with status: " + response.getStatusCode());
            }

            Map<String, Object> tokenResponse = (Map<String, Object>) response.getBody();
            String newAccessToken = (String) tokenResponse.get("access_token");

            if (newAccessToken == null || newAccessToken.isBlank()) {
                throw new RuntimeException("Token refresh response missing access_token");
            }

            // persist new token — refresh_token stays the same unless Google rotates it
            secretService.update(integration.getCredentialRef(),
                    Map.of("access_token", newAccessToken));

            return newAccessToken;

        } catch (HttpClientErrorException e) {
            // 400 invalid_grant — refresh token revoked, user must reconnect
            throw new RuntimeException(
                    "Gmail refresh token invalid for integration " + integration.getId()
                            + " — user must reconnect: " + e.getResponseBodyAsString(), e);
        }
    }

    // ── HTTP helpers ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String url, String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        var response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return response.getBody() != null ? response.getBody() : Map.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String url, String accessToken, Object body) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        return response.getBody() != null ? response.getBody() : Map.of();
    }

    // ── Functional interface for token-refresh wrapper ─────────────────────────

    @FunctionalInterface
    private interface ApiCall {
        Map<String, Object> execute(String accessToken);
    }


    @Override
    public ConnectorType getConnectorType() {
        return ConnectorType.GMAIL;
    }
}
