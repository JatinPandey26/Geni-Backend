package com.geni.backend.integration;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class InstallCallbackResult {

    // ── what goes into Integration directly ──────────────────────────
    String              name;            // "jatin-org GitHub"
    String              connectorType;   // "GITHUB"
    Long                userId;          // null in V0.1
    Map<String, Object> metadata;        // { installationId: "..." }
    String externalId;

    // ── handled separately by service ────────────────────────────────
    Map<String, Object> credentials;     // goes to SecretProvider, not Integration row

    // ── domain helper ─────────────────────────────────────────────────
    public boolean hasCredentials() {
        return credentials != null && !credentials.isEmpty();
    }
}