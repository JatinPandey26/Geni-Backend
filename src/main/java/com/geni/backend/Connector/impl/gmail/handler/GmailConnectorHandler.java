package com.geni.backend.Connector.impl.gmail.handler;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.Connector.handler.ConnectorHandler;
import com.geni.backend.Connector.impl.gmail.config.GmailConnectorConfig;
import com.geni.backend.integration.InstallCallbackResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailConnectorHandler implements ConnectorHandler {

    private static final String AUTH_URL   = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL  = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    // Scopes — read + send covers most workflow use cases
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.send",
            "https://www.googleapis.com/auth/gmail.modify",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private final GmailConnectorConfig config;

    private final RestTemplate restTemplate;

    // ── ConnectorHandler ───────────────────────────────────────────────────────

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.GMAIL;
    }

    /**
     * Step 1 of OAuth2.
     * Builds the Google consent screen URL and returns RedirectRequired.
     * The stateToken is passed through to the callback so we can verify it.
     *
     * body is ignored for Gmail — no user-supplied credentials at this stage.
     */
    @Override
    public InstallResult install(Map<String, Object> body, String stateToken) {
        String url = UriComponentsBuilder.fromPath(AUTH_URL)
                .queryParam("client_id",     config.getClientId())
                .queryParam("redirect_uri",  config.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope",         String.join(" ", SCOPES))
                .queryParam("access_type",   "offline")   // request refresh_token
                .queryParam("prompt",        "consent")   // always show consent (ensures refresh_token)
                .queryParam("state",         stateToken)
                .build()
                .toUriString();

        log.info("Gmail OAuth2 redirect initiated");
        return new InstallResult.RedirectRequired(url);
    }

    /**
     * Step 2 of OAuth2.
     * Google redirects back with ?code=...&state=...
     * Exchange the code for access_token + refresh_token.
     * Fetch user info to build a sensible integration name.
     */
    @Override
    public InstallCallbackResult handleCallback(Map<String, String> callbackParams) {
        String code  = callbackParams.get("code");
        String error = callbackParams.get("error");

        if (error != null) {
            throw new RuntimeException("Gmail OAuth2 denied by user: " + error);
        }
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Gmail OAuth2 callback missing 'code' parameter");
        }

        // Exchange code for tokens
        Map<String, Object> tokens = exchangeCodeForTokens(code);

        String accessToken  = (String) tokens.get("access_token");
        String refreshToken = (String) tokens.get("refresh_token");
        String tokenType    = (String) tokens.get("token_type");
        Object expiresIn    = tokens.get("expires_in");

        // Fetch user email to name the integration
        String userEmail = fetchUserEmail(accessToken);

        // setup Gmail watch for new email notifications (PUSH not polling)
        Map<String,Object> watchResponse = setupWatch(accessToken);

        return InstallCallbackResult.builder()
                .name(userEmail + " (Gmail)")
                .connectorType(ConnectorType.GMAIL.name())
                .externalId(userEmail)
                .metadata(Map.of(
                        "email",     userEmail,
                        "tokenType", tokenType != null ? tokenType : "Bearer",
                        "expiresIn", expiresIn != null ? expiresIn : 3600,
                        "historyId", watchResponse.get("historyId"),
                        "watchExpiresIn", watchResponse.get("expiration")
                ))
                .credentials(Map.of(
                        "access_token",  accessToken,
                        "refresh_token", refreshToken != null ? refreshToken : ""
                        // stored encrypted by SecretProvider, never in the Integration row
                ))
                .build();
    }

    /**
     * Overload for connectors that pass the raw provider response as a string.
     * Gmail doesn't need this — kept to satisfy the interface.
     */
    @Override
    public InstallCallbackResult handleCallback(Map<String, String> callbackParams,
                                                String response) {
        return handleCallback(callbackParams);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchangeCodeForTokens(String code) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var body = new LinkedMultiValueMap<String, String>();
        body.add("code",          code);
        body.add("client_id",     config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("redirect_uri", config.getRedirectUri());
        body.add("grant_type",    "authorization_code");

        var response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Gmail token exchange failed: " + response.getStatusCode());
        }

        return (Map<String, Object>) response.getBody();
    }

    private Map<String,Object> setupWatch(String accessToken){
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        Map<String, Object> requestBody = Map.of(
                "topicName", config.getGlobalTopicName(),
                "labelIds", List.of("INBOX") // optional filter
        );

        var response = restTemplate.exchange(
                config.getWatchUrl(),
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to setup Gmail watch: " + response.getStatusCode());
        }

        return (Map<String, Object>) response.getBody();
    }

    @SuppressWarnings("unchecked")
    private String fetchUserEmail(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        var response = restTemplate.exchange(
                USER_INFO_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch Gmail user info");
        }

        Map<String, Object> userInfo = (Map<String, Object>) response.getBody();
        String email = (String) userInfo.get("email");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Gmail user info did not contain an email");
        }

        return email;
    }
}
