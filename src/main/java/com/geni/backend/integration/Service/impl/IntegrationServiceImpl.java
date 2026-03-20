package com.geni.backend.integration.Service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.Connector.Service.ConnectorService;
import com.geni.backend.Connector.handler.ConnectorHandler;
import com.geni.backend.Connector.validation.ConnectorDefinitionValidator;
import com.geni.backend.common.exception.ResourceNotFoundException;
import com.geni.backend.integration.InstallCallbackResult;
import com.geni.backend.integration.Integration;
import com.geni.backend.integration.Service.IntegrationService;
import com.geni.backend.integration.endpoint.IntegrationCreateRequest;
import com.geni.backend.integration.repository.IntegrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    private final ConnectorService connectorService;
    private final ConnectorDefinitionValidator connectorDefinitionValidator;
    private final IntegrationRepository integrationRepository;

    @Override
    public InstallResult createIntegration(IntegrationCreateRequest createRequest) {

        this.connectorDefinitionValidator.validateCredentials(createRequest.getConnectorType().getType(),createRequest.getCredentialFields());

        ConnectorHandler connectorHandler = this.connectorService.getConnectorHandler(createRequest.getConnectorType());

        InstallResult result = connectorHandler.install(createRequest.getCredentialFields(),"TEST_STATE");

        if(result instanceof InstallResult.Completed){
            // at this moment we need to persist a integration into DB
            // not needed now

        }

        return result;
    }

    @Override
    public void handleCallback(String connectorType, Map<String, String> params) throws NoSuchMethodException {
        InstallCallbackResult result = this.connectorService.getConnectorHandler(ConnectorType.valueOf(connectorType)).handleCallback(params);

        // save integration from callback result.

        Integration integration = toIntegration(result);
        persist(integration);
    }

    @Override
    public void createIntegration(String connectorType, Map<String, String> params , String response) {
        InstallCallbackResult result = this.connectorService.getConnectorHandler(ConnectorType.valueOf(connectorType)).handleCallback(params,response);
        Integration integration = toIntegration(result);
        persist(integration);
    }

    @Override
    public void deleteIntegration(Specification specification) {
        Optional<Integration> integrationOptional = integrationRepository.findOne(specification);
        if(integrationOptional.isEmpty()){
            throw new ResourceNotFoundException("Integration not found -> " + specification.toString());
        }
        Integration integration = integrationOptional.get();
        integration.setEnabled(false);
        integrationRepository.save(integration);

    }

    @Override
    public List<Integration> fetchIntegrations() {
        return integrationRepository.findAll();
    }

    @Override
    public List<Integration> fetchIntegrations(ConnectorType connectorType) {
        return integrationRepository.findByConnectorType(connectorType.getType());
    }

    @Transactional
    private Integration persist(Integration integration) {

        // result carries everything — no assembly needed
        integration = integrationRepository.save(integration);

        return integration;
    }


    Integration toIntegration(InstallCallbackResult result){
        return Integration.builder()
                .name(result.getName())
                .enabled(Boolean.TRUE)
                .connectorType(result.getConnectorType())
                .credentialRef("TODO THIS") // TODO: result.getCredentials() should be transformed to ref
                .metadata(result.getMetadata())
                .externalId(result.getExternalId())
                .userId(null) // TODO : change when user layer comes in
                .build();
    }

}
