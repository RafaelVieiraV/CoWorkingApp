package ec.edu.espe.coworkingapp.web.controller;



import ec.edu.espe.coworkingapp.domain.WorkspaceType;

import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;

import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;

import ec.edu.espe.coworkingapp.service.WorkspaceService;
import ec.edu.espe.coworkingapp.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.web.PageableDefault;



@RestController

@RequestMapping("/api/workspaces")

public class WorkspaceController {



    private final WorkspaceService workspaceService;
    private final ec.edu.espe.coworkingapp.service.BookingService bookingService;



    public WorkspaceController(WorkspaceService workspaceService, ec.edu.espe.coworkingapp.service.BookingService bookingService) {

        this.workspaceService = workspaceService;
        this.bookingService = bookingService;

    }



    @PostMapping

    public ResponseEntity<WorkspaceResponseDto> create(@Valid @RequestBody WorkspaceRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.create(dto));

    }



    @GetMapping

    public ResponseEntity<List<WorkspaceResponseDto>> findAll() {

        return ResponseEntity.ok(workspaceService.findAll());

    }



    @GetMapping("/search")
    public ResponseEntity<Page<WorkspaceResponseDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) WorkspaceType type,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(workspaceService.searchPage(name, available, type, pageable));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workspaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<WorkspaceResponseDto>> findAllAvailable() {

        return ResponseEntity.ok(workspaceService.findAllAvailable());

    }



    @GetMapping("/type/{type}")

    public ResponseEntity<List<WorkspaceResponseDto>> findByType(@PathVariable WorkspaceType type) {

        return ResponseEntity.ok(workspaceService.findByType(type));

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

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        workspaceService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bookings")

    public ResponseEntity<List<ec.edu.espe.coworkingapp.dto.response.BookingResponseDto>> findWorkspaceBookings(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findByWorkspace(id));
    }

}
