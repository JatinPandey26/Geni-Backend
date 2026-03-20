package com.geni.backend.integration.endpoint;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.common.CredentialField;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class IntegrationCreateRequest {
    private ConnectorType connectorType;
    private Map<String,Object> credentialFields; //credentialField.key -> value
}
