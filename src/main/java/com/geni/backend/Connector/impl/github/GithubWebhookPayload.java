package com.geni.backend.Connector.impl.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geni.backend.common.TriggerPayload;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// GithubWebhookPayload.java
// GithubWebhookPayload.java — add installation event fields
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubWebhookPayload implements TriggerPayload {

    @JsonProperty("action")
    private String action;

    @JsonProperty("installation")
    private Installation installation;

    @JsonProperty("repositories")
    private List<RepositoryRef> repositories;   // present on installation event

    @JsonProperty("sender")
    private Actor sender;

    @JsonProperty("repository")
    private RepositoryRef repository;           // present on repo/issue events

    @JsonProperty("issue")
    private Issue issue;                       // present on issue events

    @JsonProperty("pull_request")
    private PullRequest pullRequest;               // present on PR events

    @JsonProperty("review")
    private Review review;                         // present on review events

    @JsonProperty("comment")
    private Comment comment;                       // present on comment events

    @JsonProperty("commits")
    private List<Commit> commits;                  // present on push events

    @JsonProperty("ref")
    private String ref;                            // branch ref on push

    @JsonProperty("ref_type")
    private String refType;                        // "branch" | "tag" on push

    @JsonProperty("before")
    private String before;                         // before sha on push

    @JsonProperty("after")
    private String after;                          // after sha on push

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

        @JsonProperty("issue")
        private Issue issue;

    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {

        @JsonProperty("number")
        private Integer number;

        @JsonProperty("title")
        private String title;

        @JsonProperty("body")
        private String body;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("state")
        private String state;

        @JsonProperty("labels")
        private List<Label> labels;

        @JsonProperty("assignee")
        private Actor assignee;                // who is assigned
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {

        @JsonProperty("name")
        private String name;
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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequest {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("number")
        private Integer number;

        @JsonProperty("title")
        private String title;

        @JsonProperty("body")
        private String body;

        @JsonProperty("state")
        private String state;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("user")
        private Actor user;                    // who opened the PR

        @JsonProperty("assignee")
        private Actor assignee;                // who is assigned

        @JsonProperty("labels")
        private List<Label> labels;

        @JsonProperty("base")
        private BranchRef base;                // base branch

        @JsonProperty("head")
        private BranchRef head;                // head branch

        @JsonProperty("merged")
        private Boolean merged;                // if merged
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchRef {
        @JsonProperty("ref")
        private String ref;                    // branch name

        @JsonProperty("sha")
        private String sha;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Review {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("state")
        private String state;                    // "approved" | "changes_requested" | "commented"

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("user")
        private Actor user;                    // who submitted the review
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("body")
        private String body;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("user")
        private Actor user;                    // who made the comment
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        @JsonProperty("id")
        private String id;                      // commit SHA

        @JsonProperty("message")
        private String message;

        @JsonProperty("url")
        private String url;

        @JsonProperty("author")
        private Actor author;                  // who made the commit
    }
}