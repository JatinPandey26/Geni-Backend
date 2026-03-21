package com.geni.backend.Connector.impl.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

// GithubAccessToken.java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAccessToken {

    @JsonProperty("token")
    private String token;               // ghs_... — this is the installation access token

    @JsonProperty("expires_at")
    private Instant expiresAt;          // always 1hr from now

    @JsonProperty("permissions")
    private Map<String, String> permissions;

    @JsonProperty("repository_selection")
    private String repositorySelection;
}