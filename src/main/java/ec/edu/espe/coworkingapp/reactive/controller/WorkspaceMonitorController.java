package ec.edu.espe.coworkingapp.reactive.controller;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.service.WorkspaceMonitorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/monitor")
public class WorkspaceMonitorController {

    private final WorkspaceMonitorService service;

    public WorkspaceMonitorController(WorkspaceMonitorService service) {
        this.service = service;
    }

    // Listar todas las lecturas reales (devuelve [] si no hay)
    @GetMapping("/readings")
    public Flux<WorkspaceReading> getAllReadings() {
        return service.getAllReadings();
    }

    // Stream en tiempo real (SSE): aquí llegan las lecturas que emiten las reservas reales
    @GetMapping(value = "/readings/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WorkspaceReading> getLiveStream() {
        return service.getLiveStream();
    }

    // Promedio GLOBAL asíncrono (~3s), no bloqueante
    @GetMapping("/readings/average")
    public Mono<Double> getAverageOccupancy() {
        return service.getAverageOccupancy();
    }

    // Promedio de una sala específica (~1.5s)
    @GetMapping("/readings/{workspaceId}/average")
    public Mono<Double> getAverageByWorkspace(@PathVariable String workspaceId) {
        return service.getAverageByWorkspace(workspaceId);
    }

    // (Lab) Generación automática con Flux.interval, disponible para Postman/informe
    @GetMapping("/readings/generate")
    public Mono<String> generateReadings() {
        return service.generateReadings();
    }
}