package com.geni.backend.Connector.impl.gmail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "geni.connectors.gmail")
@Data
public class GmailConnectorConfig {
    String clientId;
    String clientSecret;
    private String globalTopicName;
    private String watchUrl;
    private String redirectUri;
}
