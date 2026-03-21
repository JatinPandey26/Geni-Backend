package com.geni.backend.workflow.endpoint;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.workflow.core.ActionDefinitionResponse;
import com.geni.backend.workflow.service.ActionDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/actions")
@RequiredArgsConstructor
public class ActionDefinitionEndpoint {

    private final ActionDefinitionService service;

    // GET /api/v1/actions
    // GET /api/v1/actions?connectorType=GMAIL
    @GetMapping
    public List<ActionDefinitionResponse> list(
            @RequestParam(required = false) ConnectorType connectorType) {

        return connectorType != null
                ? service.listByConnector(connectorType)
                : service.listAll();
    }

    // GET /api/v1/actions/{type}
    // e.g. GET /api/v1/actions/GMAIL_SEND_EMAIL
    // Returns full inputSchema + outputSchema so the UI can render the field mapper
    @GetMapping("/{type}")
    public ActionDefinitionResponse get(@PathVariable String type) {
        return service.getByType(type);
    }

}
