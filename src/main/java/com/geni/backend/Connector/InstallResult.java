package com.geni.backend.Connector;

import java.util.Map;

// InstallResult.java
// Handler returns one of two outcomes — controller acts on it
public sealed interface InstallResult {

    // Handler stored everything — integration is ready
    record Completed(
            Map<String, String> credentials,
            Map<String, Object> metadata,
            String suggestedName
    ) implements InstallResult {}

    // Handler needs user to go somewhere — redirect them
    record RedirectRequired(
            String redirectUrl
    ) implements InstallResult {}
}
