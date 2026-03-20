package com.geni.backend.Connector.github.service;

import com.geni.backend.Connector.github.GithubWebhookPayload;

import java.util.Map;

public interface GithubService {

    public void handleEvent(Map<String,String> params, String rawBody);
    GithubWebhookPayload parse(String rawBody);

}
