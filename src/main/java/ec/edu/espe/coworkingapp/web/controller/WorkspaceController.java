package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequest;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponse;
import ec.edu.espe.coworkingapp.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> findAll() {
        return ResponseEntity.ok(workspaceService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<WorkspaceResponse>> findAvailable() {
        return ResponseEntity.ok(workspaceService.findAvailable());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workspaceService.findById(id));
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(@Valid @RequestBody WorkspaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody WorkspaceRequest request) {
        return ResponseEntity.ok(workspaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workspaceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}