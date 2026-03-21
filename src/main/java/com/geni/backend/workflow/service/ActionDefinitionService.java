package com.geni.backend.workflow.service;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.workflow.core.ActionDefinitionResponse;

import java.util.List;

public interface ActionDefinitionService {

    List<ActionDefinitionResponse> listAll();

    List<ActionDefinitionResponse> listByConnector(ConnectorType connectorType);

    ActionDefinitionResponse getByType(String type);
}