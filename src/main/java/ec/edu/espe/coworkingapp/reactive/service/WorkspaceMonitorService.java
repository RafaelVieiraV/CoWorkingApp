package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.repository.WorkspaceReadingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WorkspaceMonitorService {

    private final WorkspaceReadingRepository repository;

    public WorkspaceMonitorService(WorkspaceReadingRepository repository) {
        this.repository = repository;
    }

    // Guarda una lectura y la devuelve como Mono
    public Mono<WorkspaceReading> saveReading(WorkspaceReading reading) {
        return Mono.fromCallable(() -> repository.save(reading))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Todas las lecturas como Flux (devuelve vacío si no hay)
    public Flux<WorkspaceReading> getAllReadings() {
        return repository.findAll();
    }

    // Stream en vivo (hot stream)
    public Flux<WorkspaceReading> getLiveStream() {
        return repository.getLiveStream();
    }

    // Promedio GLOBAL de ocupación: asíncrono (~3s) y NO bloqueante.
    // El Thread.sleep corre en boundedElastic, no en el event-loop / hilo de la petición.
    public Mono<Double> getAverageOccupancy() {
        return repository.findAll()
                .map(WorkspaceReading::getOccupancyPercentage)
                .collectList()
                .flatMap(list -> Mono.fromCallable(() -> {
                    Thread.sleep(3000); // simula operación costosa
                    return list.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    // ACTIVIDAD 2: promedio de ocupación de UN workspace específico (~1.5s, asíncrono).
    public Mono<Double> getAverageByWorkspace(String workspaceId) {
        return repository.findAll()
                .filter(r -> workspaceId.equals(r.getWorkspaceId())) // solo ese workspace
                .map(WorkspaceReading::getOccupancyPercentage)
                .collectList()
                .flatMap(list -> Mono.fromCallable(() -> {
                    Thread.sleep(1500); // retardo asíncrono
                    return list.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    // ACTIVIDAD 3: genera lecturas automáticas (1 por segundo durante 10s) con Flux.interval
    // y las emite al stream existente. Devuelve un mensaje al terminar.
    public Mono<String> generateReadings() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(10) // 10 lecturas => ~10 segundos
                .map(i -> {
                    String workspaceId = "sala-" + ThreadLocalRandom.current().nextInt(1, 4); // sala-1..3
                    double occupancy = Math.round(ThreadLocalRandom.current().nextDouble(0, 100) * 100.0) / 100.0;
                    return new WorkspaceReading(workspaceId, occupancy, LocalDateTime.now());
                })
                .doOnNext(repository::save) // guarda y emite cada lectura al stream en vivo
                .then(Mono.just("Generación finalizada: 10 lecturas emitidas al stream"));
    }
}