package com.geni.backend.Connector.impl.github.client;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.client.ConnectorClient;
import com.geni.backend.Connector.impl.github.config.GithubAppConfig;
import com.geni.backend.integration.Integration;
import com.geni.backend.secret.service.SecretService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GithubConnectorClient implements ConnectorClient {

    private static final String GITHUB_API = "https://api.github.com";

    private final RestTemplate restTemplate;
    private final SecretService secretService;
    private final GithubAppConfig config;

    // ── PUBLIC API ─────────────────────────────────────────────────────────────

    public Map<String, Object> createIssueComment(Integration integration,
                                                  String owner,
                                                  String repo,
                                                  String issueNumber,
                                                  String body) {

        return executeWithAccessToken(integration, token ->
                post("/repos/" + owner + "/" + repo + "/issues/" + issueNumber + "/comments",
                        token,
                        Map.of("body", body))
        );
    }

    public Map<String, Object> createIssue(Integration integration,
                                           String owner,
                                           String repo,
                                           Map<String, Object> payload) {

        return executeWithAccessToken(integration, token ->
                post("/repos/" + owner + "/" + repo + "/issues",
                        token,
                        payload)
        );
    }

    // ── CORE TOKEN FLOW ────────────────────────────────────────────────────────

    private Map<String, Object> executeWithAccessToken(
            Integration integration,
            ApiCall apiCall) {

        String installationId = String.valueOf(integration.getMetadata().get("installationId"));

        if(installationId == null){
            log.error("Installation Id not found for Github Integration : {}" , integration.getId());
            throw new RuntimeException("Installation Id not found for Github Integration");
        }

        Map<String, Object> credentials =
                secretService.getSecret(integration.getCredentialRef(), Map.class);

        String accessToken = String.valueOf(credentials.get("token"));


        try{
            return apiCall.execute(accessToken);
        }
        catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                throw e;
            }

            log.debug("Access token for github integration {} expired refetching token and retrying the operation" , integration.getId());

            Map<String,Object> accessTokenResponse = getAccessToken(installationId);
            if (accessTokenResponse == null || accessTokenResponse.get("token") == null){
                log.error("Access token not found for Github Integration : {}" , integration.getId());
                throw new RuntimeException("Access token not found for Github Integration");
            }

            accessToken = accessTokenResponse.get("token").toString();
            return apiCall.execute(accessToken);
        }
    }

    public Map<String,Object> getAccessToken(String installationId) {

        String jwt = generateJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.set("Accept", "application/vnd.github+json");

        ResponseEntity<Map> response = restTemplate.exchange(
                GITHUB_API + "/app/installations/" + installationId + "/access_tokens",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(), headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch installation token");
        }

        return response.getBody();
    }

    // ── JWT GENERATION ─────────────────────────────────────────────────────────

    private String generateJwt() {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setIssuedAt(new Date(now - 60000))
                .setExpiration(new Date(now + (10 * 60 * 1000)))
                .setIssuer(config.getAppId())
                .signWith(config.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    // ── HTTP HELPERS ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, String token, Object body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/vnd.github+json");

        ResponseEntity<Map> response = restTemplate.exchange(
                GITHUB_API + path,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        return response.getBody() != null ? response.getBody() : Map.of();
    }

    // ── FUNCTIONAL INTERFACE ───────────────────────────────────────────────────

    @FunctionalInterface
    private interface ApiCall {
        Map<String, Object> execute(String token);
    }

    @Override
    public ConnectorType getConnectorType() {
        return ConnectorType.GITHUB;
    }
}