package com.geni.backend.Connector.github;

import java.util.Arrays;

// GithubWebhookEvent.java
public enum GithubWebhookEvent {

    ISSUES          ("issues"),
    PULL_REQUEST    ("pull_request"),
    ISSUE_COMMENT   ("issue_comment"),
    PUSH            ("push"),
    INSTALLATION    ("installation"),
    PING            ("ping"),
    UNKNOWN         ("unknown");

    private final String headerValue;

    GithubWebhookEvent(String headerValue) {
        this.headerValue = headerValue;
    }

    public static GithubWebhookEvent from(String header) {
        if (header == null) return UNKNOWN;
        return Arrays.stream(values())
                .filter(e -> e.headerValue.equals(header))
                .findFirst()
                .orElse(UNKNOWN);
    }
}