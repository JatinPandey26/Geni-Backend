package com.geni.backend.workflow.core;

public enum ActionType {
    GMAIL_SEND_EMAIL("GMAIL_SEND_EMAIL", "gmail_send_email"),
    GITHUB_CREATE_ISSUE("GITHUB_CREATE_ISSUE", "github_create_issue"),
    GITHUB_CREATE_PULL_REQUEST("GITHUB_CREATE_PULL_REQUEST", "github_create_pull_request");

    String type;
    String name;

    ActionType(String type, String name) {
        this.type = type;
        this.name = name;
    }

}
