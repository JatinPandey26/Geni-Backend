package com.geni.backend.Connector.impl.github;

import java.util.Arrays;

// GithubWebhookEvent.java
public enum GithubWebhookEvent {

    ISSUE_OPENED          ("issues"),
    ISSUE_CLOSED          ("issue_closed"),
    ISSUE_REOPENED        ("issue_reopened"),
    ISSUE_ASSIGNED        ("issue_assigned"),
    ISSUE_UPDATED         ("issue_updated"),
    PULL_REQUEST_OPENED    ("pull_request_opened"),
    PULL_REQUEST_MERGED    ("pull_request_merged"),
    PULL_REQUEST_REVIEW_REQUESTED ("pull_request_review_requested"),
    PULL_REQUEST_REVIEW_SUBMITTED ("pull_request_review_submitted"),
    PULL_REQUEST_REVIEW_COMMENT ("pull_request_review_comment"),
    ISSUE_COMMENT   ("issue_comment"),
    PUSH            ("push"),
    INSTALLATION    ("installation"),
    PING            ("ping"),
    BRANCH_CREATE      ("branch_create"),
    TAG_CREATE         ("tag_create"),
    UNKNOWN         ("unknown");

    private final String event;

    GithubWebhookEvent(String headerValue) {
        this.event = headerValue;
    }

    public static GithubWebhookEvent from(String event,GithubWebhookPayload payload) {
        if (event == null) return UNKNOWN;

        // create event
        if(event.equals("create")){
            if(payload.getRef() != null && payload.getRefType() != null && payload.getRefType().equals("branch")){
                return BRANCH_CREATE;
            }
            if(payload.getRef() != null && payload.getRefType() != null && payload.getRefType().equals("tag")){
                return TAG_CREATE;
            }
        }

        // issue events have "action" field that further specifies the event type
        if (event.equals("issues") && payload.getAction() != null) {
            switch (payload.getAction()) {
                case "assigned":
                    return ISSUE_ASSIGNED;
                case "opened":
                    return ISSUE_OPENED;
                case "closed":
                    return ISSUE_CLOSED;
                case "reopened":
                    return ISSUE_REOPENED;
                case "edited":
                    return ISSUE_UPDATED;
                // add more issue actions as needed
            }
        }

        // PR events have "action" field that further specifies the event type
        if (event.equals("pull_request") && payload.getAction() != null) {
            switch (payload.getAction()) {
                case "opened":
                    return PULL_REQUEST_OPENED;
                case "closed":
                    if (Boolean.TRUE.equals(payload.getPullRequest().getMerged())) {
                        return PULL_REQUEST_MERGED;
                    } else {
                        return UNKNOWN; // PR closed but not merged, no trigger
                    }
                case "review_requested":
                    return PULL_REQUEST_REVIEW_REQUESTED;
            }
        }

        // PR review events
        if (event.equals("pull_request_review") && payload.getAction() != null) {
            switch (payload.getAction()) {
                case "submitted":
                    return PULL_REQUEST_REVIEW_SUBMITTED;
            }
        }

        // PR review comment events
        if (event.equals("pull_request_review_comment") && payload.getAction() != null) {
            switch (payload.getAction()) {
                case "created":
                    return PULL_REQUEST_REVIEW_COMMENT;
            }
        }

        // Issue comment events
        if (event.equals("issue_comment") && payload.getAction() != null) {
            switch (payload.getAction()) {
                case "created":
                    return ISSUE_COMMENT;
            }
        }

        
        return Arrays.stream(values())
                .filter(e -> e.event.equals(event))
                .findFirst()
                .orElse(UNKNOWN);
    }
}