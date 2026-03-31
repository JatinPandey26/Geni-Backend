package com.geni.backend.integration.Service;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.InstallResult;
import com.geni.backend.integration.Integration;
import com.geni.backend.integration.endpoint.IntegrationCreateRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface IntegrationService {

    public InstallResult createIntegration(IntegrationCreateRequest createRequest);
    public void handleCallback(String connectorType , Map<String,String> params) throws NoSuchMethodException;
    public void createIntegration(String connectorType, Map<String, String> params);
    public void createIntegration(String connectorType, Map<String, String> params, String response);
    public void deleteIntegration(Specification specification);
    public List<Integration> fetchIntegrations();
    public List<Integration> fetchIntegrations(ConnectorType connectorType);
    public Integration fetchIntegration(Long id);
}
