package com.example.u1proyecto_grupob.web.controller;

import com.example.u1proyecto_grupob.domain.WorkspaceType;
import com.example.u1proyecto_grupob.dto.WorkspaceRequestDto;
import com.example.u1proyecto_grupob.dto.WorkspaceResponseDto;
import com.example.u1proyecto_grupob.service.WorkspaceService;
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

    @PostMapping
    public ResponseEntity<WorkspaceResponseDto> create(@Valid @RequestBody WorkspaceRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponseDto>> findAll() {
        return ResponseEntity.ok(workspaceService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<WorkspaceResponseDto>> findAllAvailable() {
        return ResponseEntity.ok(workspaceService.findAllAvailable());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workspaceService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponseDto> update(@PathVariable Long id, @Valid @RequestBody WorkspaceRequestDto dto) {
        return ResponseEntity.ok(workspaceService.update(id, dto));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        workspaceService.disable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<WorkspaceResponseDto>> findByType(@PathVariable String type) {
        WorkspaceType wt = WorkspaceType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(workspaceService.findByType(wt));
    }
}
