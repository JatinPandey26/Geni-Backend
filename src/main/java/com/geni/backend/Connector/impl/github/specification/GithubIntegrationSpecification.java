package com.geni.backend.Connector.impl.github.specification;

import com.geni.backend.integration.Integration;
import com.geni.backend.integration.repository.specification.IntegrationSpecification;
import com.geni.backend.integration.repository.specification.MetadataSpecification;
import org.springframework.data.jpa.domain.Specification;

public class GithubIntegrationSpecification {

    private static final String CONNECTOR_TYPE = "GITHUB";

    public static Specification<Integration> isGithub() {
        return IntegrationSpecification.hasConnectorType(CONNECTOR_TYPE);
    }

    public static Specification<Integration> hasInstallationId(String installationId) {
        return isGithub()
                .and(MetadataSpecification.hasMetadataValue("installationId", installationId));
    }

    public static Specification<Integration> hasAccountLogin(String login) {
        return isGithub()
                .and(MetadataSpecification.hasMetadataValue("accountLogin", login));
    }

    public static Specification<Integration> hasAccountType(String type) {
        return isGithub()
                .and(MetadataSpecification.hasMetadataValue("accountType", type));
    }
}