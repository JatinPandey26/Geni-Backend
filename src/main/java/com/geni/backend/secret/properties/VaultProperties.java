package com.geni.backend.secret.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "secret.vault")
@Data
public class VaultProperties {
    private String uri;
    private String token;
    private String deleteUri;
}
