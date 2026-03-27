package com.geni.backend.integration.validator;


import com.geni.backend.common.exception.WorkflowValidationException;
import com.geni.backend.integration.Integration;
import com.geni.backend.integration.repository.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationValidator {

    private final IntegrationRepository repo;

    /**
     * Throws if the integration does not exist.
     * Returns the found integration so callers can use it without a second lookup.
     */
    public Integration validateExists(long integrationId) {
        return repo.findById(integrationId)
                .orElseThrow(() -> new WorkflowValidationException(
                        "Integration not found: '" + integrationId + "'"));
    }

    /**
     * Validates existence and checks the integration is still ACTIVE.
     */
    public Integration validateActive(long integrationId) {
        var integration = validateExists(integrationId);

        if (!integration.isEnabled()) {
            throw new WorkflowValidationException(
                    "Integration '" + integrationId + " -> " + integration.getName()
                            + "' is not enabled. Please reconnect the account.");
        }

        return integration;
    }

    public Integration validateActiveWithConnector(long integrationId, String connectorType) {
        var integration = validateActive(integrationId);

        if (!integration.getConnectorType().equals(connectorType)) {
            throw new WorkflowValidationException(
                    "Integration '" + integrationId + " -> " + integration.getName()
                            + "' is for connector '" + integration.getConnectorType()
                            + "' but was used for connector '" + connectorType + "'.");
        }

        return integration;
    }
}

