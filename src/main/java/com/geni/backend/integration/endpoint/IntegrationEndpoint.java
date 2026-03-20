package com.geni.backend.integration.endpoint;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.common.ApiResponse;
import com.geni.backend.integration.Integration;
import com.geni.backend.integration.Service.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * REST endpoint for managing integrations with third-party services.
 * Provides endpoints to create, retrieve, and handle callbacks for various connector types.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationEndpoint {

    private final IntegrationService integrationService;

    /**
     * Creates a new integration with the specified connector type and credentials.
     *
     * @param integrationCreateRequest the request containing connector type and credential fields
     * @return ApiResponse containing the InstallResult with integration details
     */
    @PostMapping("/create")
    public ApiResponse<InstallResult> createIntegration(@RequestBody IntegrationCreateRequest integrationCreateRequest) {
        log.info("Creating new integration with connector type: {}", 
                 integrationCreateRequest.getConnectorType());
        log.debug("Integration creation request details - Connector: {}, Credentials fields count: {}", 
                  integrationCreateRequest.getConnectorType(), 
                  integrationCreateRequest.getCredentialFields() != null ? 
                  integrationCreateRequest.getCredentialFields().size() : 0);

        if (integrationCreateRequest == null || 
            integrationCreateRequest.getConnectorType() == null) {
            log.warn("Invalid integration creation request received - missing required fields");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                            "Connector type is required");
        }

        try {
            InstallResult installResult = this.integrationService.createIntegration(integrationCreateRequest);
            log.info("Integration created successfully with connector type: {}", 
                     integrationCreateRequest.getConnectorType());
            log.debug("Install result: {}", installResult);
            return ApiResponse.ok(installResult);
        } catch (Exception e) {
            log.error("Error creating integration for connector type: {}", 
                      integrationCreateRequest.getConnectorType(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                                            "Failed to create integration", e);
        }
    }

    /**
     * Handles OAuth/callback responses from third-party services.
     *
     * @param connectorType the type of connector (e.g., GITHUB, SLACK)
     * @param params the callback parameters (e.g., authorization code)
     * @throws NoSuchMethodException if the connector type handler is not found
     */
    @PostMapping("/{connectorType}/callback")
    public void handleCallback(@PathVariable String connectorType,
                               @RequestParam Map<String, String> params) throws NoSuchMethodException {
        log.info("Handling callback for connector type: {}", connectorType);
        log.debug("Callback parameters: {}", params);

        if (connectorType == null || connectorType.trim().isEmpty()) {
            log.warn("Invalid callback request - empty connector type");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                            "Connector type is required");
        }

        try {
            // Validate connector type exists
            ConnectorType.valueOf(connectorType.toUpperCase());
            integrationService.handleCallback(connectorType, params);
            log.info("Callback processed successfully for connector type: {}", connectorType);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid connector type provided in callback: {}", connectorType, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                            "Invalid connector type: " + connectorType, e);
        } catch (NoSuchMethodException e) {
            log.error("No handler method found for connector type: {}", connectorType, e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing callback for connector type: {}", connectorType, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                                            "Failed to process callback", e);
        }
    }

    /**
     * Retrieves all integrations across all connector types.
     *
     * @return ApiResponse containing list of all integrations
     */
    @GetMapping
    public ApiResponse<List<Integration>> getAllIntegration() {
        log.info("Fetching all integrations");
        
        try {
            List<Integration> integrations = integrationService.fetchIntegrations();
            log.info("Successfully fetched {} integrations", integrations.size());
            log.debug("Integration list: {}", integrations);
            return ApiResponse.ok(integrations);
        } catch (Exception e) {
            log.error("Error fetching all integrations", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                                            "Failed to fetch integrations", e);
        }
    }

    /**
     * Retrieves all integrations for a specific connector type.
     *
     * @param connectorType the connector type to filter by
     * @return ApiResponse containing list of integrations for the specified connector type
     */
    @GetMapping("/{connectorType}")
    public ApiResponse<List<Integration>> getAllIntegrationByConnectorType(
            @PathVariable(name = "connectorType") String connectorType) {
        log.info("Fetching integrations for connector type: {}", connectorType);

        if (connectorType == null || connectorType.trim().isEmpty()) {
            log.warn("Invalid request - empty connector type provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                            "Connector type is required");
        }

        try {
            ConnectorType type = ConnectorType.valueOf(connectorType.toUpperCase());
            List<Integration> integrations = integrationService.fetchIntegrations(type);
            log.info("Successfully fetched {} integrations for connector type: {}", 
                     integrations.size(), connectorType);
            log.debug("Integrations for {}: {}", connectorType, integrations);
            return ApiResponse.ok(integrations);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid connector type provided: {}", connectorType, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                            "Invalid connector type: " + connectorType, e);
        } catch (Exception e) {
            log.error("Error fetching integrations for connector type: {}", connectorType, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                                            "Failed to fetch integrations for type: " + connectorType, e);
        }
    }
}
