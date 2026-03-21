package com.geni.backend.workflow.endpoint;

import com.geni.backend.workflow.core.CreateWorkflowRequest;
import com.geni.backend.workflow.core.UpdateStatusRequest;
import com.geni.backend.workflow.core.WorkflowDefinitionResponse;
import com.geni.backend.workflow.service.WorkflowDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowDefinitionEndpoint {

    private final WorkflowDefinitionService service;

    // POST /api/v1/workflows
    @PostMapping
    public ResponseEntity<WorkflowDefinitionResponse> create(
            @Valid @RequestBody CreateWorkflowRequest request) {

        var created  = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // GET /api/v1/workflows
    @GetMapping
    public List<WorkflowDefinitionResponse> list() {
        return service.listAll();
    }

    // GET /api/v1/workflows/{id}
    @GetMapping("/{id}")
    public WorkflowDefinitionResponse get(@PathVariable UUID id) {
        return service.getById(id);
    }

    // PUT /api/v1/workflows/{id}
    @PutMapping("/{id}")
    public WorkflowDefinitionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWorkflowRequest request) {
        return service.update(id, request);
    }

    // PATCH /api/v1/workflows/{id}/status
    @PatchMapping("/{id}/status")
    public WorkflowDefinitionResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return service.updateStatus(id, request.getStatus());
    }

    // DELETE /api/v1/workflows/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}