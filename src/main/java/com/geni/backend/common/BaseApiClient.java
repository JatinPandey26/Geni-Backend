package com.geni.backend.common;

import com.geni.backend.common.exception.ExternalApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

// BaseApiClient.java
public abstract class BaseApiClient {

    protected final RestClient restClient;

    protected BaseApiClient(RestClient.Builder builder, String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    protected <T> T get(String path, String bearerToken, Class<T> responseType) {
        return restClient.get()
                .uri(path)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException(baseUrlHost(), res.getStatusCode().value(),
                            new String(res.getBody().readAllBytes()));
                })
                .body(responseType);
    }

    protected <T> T post(String path, String bearerToken,
                         Object body, Class<T> responseType) {
        return restClient.post()
                .uri(path)
                .header("Authorization", "Bearer " + bearerToken)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException(baseUrlHost(), res.getStatusCode().value(),
                            new String(res.getBody().readAllBytes()));
                })
                .body(responseType);
    }

    // For OAuth token exchange endpoints — no auth header
    protected <T> T postPublic(String url, Object body, Class<T> responseType) {
        return RestClient.create()
                .post()
                .uri(url)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalApiException(url, res.getStatusCode().value(),
                            new String(res.getBody().readAllBytes()));
                })
                .body(responseType);
    }

    protected abstract String baseUrlHost(); // e.g. "api.github.com"
}