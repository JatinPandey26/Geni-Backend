package com.geni.backend.workflow.service.impl;


import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.workflow.core.ActionDefinition;
import com.geni.backend.workflow.core.ActionDefinitionRegistry;
import com.geni.backend.workflow.core.ActionDefinitionResponse;
import com.geni.backend.workflow.service.ActionDefinitionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionDefinitionServiceImpl implements ActionDefinitionService {

    private final ActionDefinitionRegistry registry;

    @Override
    public List<ActionDefinitionResponse> listAll() {
        return registry.all().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ActionDefinitionResponse> listByConnector(ConnectorType connectorType) {
        return registry.forConnector(connectorType).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ActionDefinitionResponse getByType(String type) {
        return registry.find(type)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ActionDefinition not found: " + type));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private ActionDefinitionResponse toResponse(
            ActionDefinition def) {
        return ActionDefinitionResponse.builder()
                .type(def.getType())
                .displayName(def.getDisplayName())
                .description(def.getDescription())
                .connectorType(def.getConnectorType())
                .requiresIntegration(def.isRequiresIntegration())
                .inputSchema(def.getInputSchema())
                .outputSchema(def.getOutputSchema())
                .build();
    }
}
