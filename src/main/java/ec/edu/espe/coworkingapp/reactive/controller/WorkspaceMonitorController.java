package ec.edu.espe.coworkingapp.reactive.controller;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.service.OccupancyPublisher;
import ec.edu.espe.coworkingapp.reactive.service.WorkspaceMonitorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class WorkspaceMonitorController {

    private final WorkspaceMonitorService service;
    private final OccupancyPublisher occupancyPublisher;

    public WorkspaceMonitorController(WorkspaceMonitorService service,
                                      OccupancyPublisher occupancyPublisher) {
        this.service = service;
        this.occupancyPublisher = occupancyPublisher;
    }

    // ====== Endpoints que consume el front integrado (datos REALES) ======

    // Listar todas las lecturas reales emitidas (devuelve [] si no hay)
    @GetMapping("/readings")
    public Flux<WorkspaceReading> getAllReadings() {
        return service.getAllReadings();
    }

    // Stream en tiempo real (SSE): aquí llegan las lecturas que emiten las reservas reales
    @GetMapping(value = "/readings/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WorkspaceReading> getLiveStream() {
        return service.getLiveStream();
    }

    // Promedio GLOBAL de ocupación: asíncrono (~3s), no bloqueante
    @GetMapping("/readings/average")
    public Mono<Double> getAverageOccupancy() {
        return service.getAverageOccupancy();
    }

    // Promedio de ocupación de UNA sala específica (~1.5s)
    @GetMapping("/readings/{workspaceId}/average")
    public Mono<Double> getAverageByWorkspace(@PathVariable String workspaceId) {
        return service.getAverageByWorkspace(workspaceId);
    }

    // SNAPSHOT: recalcula la ocupación REAL de TODAS las salas desde la BD y la emite al stream.
    // Sirve para ver el estado actual sin tener que confirmar/cancelar una reserva en ese instante.
    @PostMapping("/refresh")
    public List<WorkspaceReading> refreshOccupancy() {
        return occupancyPublisher.publishAll();
    }

    // ====== Endpoints del LAB (quedan disponibles para demostrar en Postman / informe) ======

    // (Lab) Generación automática con Flux.interval: 1 lectura/seg durante 10s
    @GetMapping("/readings/generate")
    public Mono<String> generateReadings() {
        return service.generateReadings();
    }
}