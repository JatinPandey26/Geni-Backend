package com.geni.backend.Connector.impl.github.service;

import com.geni.backend.Connector.impl.github.payload.GithubWebhookPayload;

import java.util.Map;

public interface GithubService {

    public void handleEvent(Map<String,String> params, String rawBody);
    GithubWebhookPayload parse(String rawBody);

}
