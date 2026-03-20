package com.geni.backend.Connector.endpoint;

import com.geni.backend.Connector.ConnectorDefinition;
import com.geni.backend.Connector.Service.ConnectorService;
import com.geni.backend.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
public class ConnectorEndpoint {

    private final ConnectorService connectorService;

    @GetMapping
    public ApiResponse<List<ConnectorDefinition>> getAll() {
        return ApiResponse.ok(connectorService.getAllDefinitions());
    }

    @GetMapping("/{connectorType}")
    public ApiResponse<ConnectorDefinition> getOne(@PathVariable String connectorType) {
        return ApiResponse.ok(connectorService.getDefinition(connectorType));
    }
}

