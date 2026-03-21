package com.geni.backend.Connector.impl.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// GithubWebhookPayload.java
// GithubWebhookPayload.java — add installation event fields
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubWebhookPayload {

    @JsonProperty("action")
    private String action;

    @JsonProperty("installation")
    private Installation installation;

    @JsonProperty("repositories")
    private List<RepositoryRef> repositories;   // present on installation event

    @JsonProperty("sender")
    private Actor sender;



    // ── Nested DTOs ───────────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Installation {

        @JsonProperty("id")
        private Long id;                        // 117765691

        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("account")
        private Actor account;                  // who installed

        @JsonProperty("app_id")
        private Long appId;                     // 3140015

        @JsonProperty("app_slug")
        private String appSlug;                 // "geni-github-manager"

        @JsonProperty("repository_selection")
        private String repositorySelection;     // "all" | "selected"

        @JsonProperty("permissions")
        private Map<String, String> permissions; // { "issues": "write", ... }

        @JsonProperty("events")
        private List<String> events;            // subscribed events

        @JsonProperty("created_at")
        private Instant createdAt;

        @JsonProperty("updated_at")
        private Instant updatedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryRef {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;                    // "feb"

        @JsonProperty("full_name")
        private String fullName;                // "JatinPersonal26/feb"

        @JsonProperty("private")
        private boolean isPrivate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Actor {
        @JsonProperty("login")
        private String login;

        @JsonProperty("id")
        private Long id;

        @JsonProperty("type")
        private String type;                    // "User" | "Organization"

        @JsonProperty("avatar_url")
        private String avatarUrl;
    }
}