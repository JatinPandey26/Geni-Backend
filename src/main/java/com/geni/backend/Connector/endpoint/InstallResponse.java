package com.geni.backend.Connector.endpoint;

import lombok.Builder;
import lombok.Value;

public sealed interface InstallResponse {

    @Value
    @Builder
    class Completed implements InstallResponse {
        String              status = "COMPLETED";
//        IntegrationResponse integration;
    }

    @Value
    @Builder
    class RedirectRequired implements InstallResponse {
        String status      = "REDIRECT_REQUIRED";
        String redirectUrl;
    }
}