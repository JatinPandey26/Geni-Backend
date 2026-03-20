package com.geni.backend.Connector.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// GithubInstallation.java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)   // GitHub returns many fields we don't need
public class GithubInstallation {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("account")
    private GithubAccount account;

    @JsonProperty("app_id")
    private Long appId;

    @JsonProperty("repository_selection")
    private String repositorySelection;   // "all" or "selected"

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubAccount {
        @JsonProperty("login")
        private String login;             // "jatin-org" ← this is what we use for display name

        @JsonProperty("id")
        private Long id;

        @JsonProperty("type")
        private String type;              // "Organization" or "User"

        @JsonProperty("avatar_url")
        private String avatarUrl;
    }
}