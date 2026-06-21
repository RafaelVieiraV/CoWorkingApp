package ec.edu.espe.coworkingapp.reactive.controller;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.service.WorkspaceMonitorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/monitor")
public class WorkspaceMonitorController {

    private final WorkspaceMonitorService service;

    public WorkspaceMonitorController(WorkspaceMonitorService service) {
        this.service = service;
    }

    // Recalcula la ocupación REAL de todas las salas y la emite al stream
    @PostMapping("/refresh")
    public Flux<WorkspaceReading> refreshOccupancy() {
        return service.refreshRealOccupancy();
    }

    // Registrar una nueva lectura de ocupación
    @PostMapping("/readings")
    public Mono<ResponseEntity<WorkspaceReading>> createReading(@RequestBody WorkspaceReading reading) {
        reading.setTimestamp(LocalDateTime.now());
        return service.saveReading(reading)
                .map(saved -> ResponseEntity.ok(saved));
    }

    // Obtener todas las lecturas
    @GetMapping("/readings")
    public Flux<WorkspaceReading> getAllReadings() {
        return service.getAllReadings();
    }

    // Stream en tiempo real usando SSE
    @GetMapping(value = "/readings/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WorkspaceReading> getLiveStream() {
        return service.getLiveStream();
    }

    // Promedio de ocupación (asíncrono)
    @GetMapping("/readings/average")
    public Mono<Double> getAverageOccupancy() {
        return service.getAverageOccupancy();
    }
}