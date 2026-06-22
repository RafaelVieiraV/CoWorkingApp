package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.repository.WorkspaceReadingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WorkspaceMonitorService {

    private final WorkspaceReadingRepository repository;

    public WorkspaceMonitorService(WorkspaceReadingRepository repository) {
        this.repository = repository;
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
    public Mono<Double> getAverageOccupancy() {
        return repository.findAll()
                .map(WorkspaceReading::getOccupancyPercentage)
                .collectList()
                .flatMap(list -> Mono.fromCallable(() -> {
                    Thread.sleep(3000); // simula operación costosa, fuera del hilo de la petición
                    return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    // Promedio de ocupación de UNA sala específica (~1.5s, asíncrono).
    public Mono<Double> getAverageByWorkspace(String workspaceId) {
        return repository.findAll()
                .filter(r -> workspaceId.equals(r.getWorkspaceId()))
                .map(WorkspaceReading::getOccupancyPercentage)
                .collectList()
                .flatMap(list -> Mono.fromCallable(() -> {
                    Thread.sleep(1500);
                    return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    // (Lab) Generación automática con Flux.interval: 1 lectura/seg durante 10s.
    public Mono<String> generateReadings() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(10)
                .map(i -> {
                    String workspaceId = "sala-" + ThreadLocalRandom.current().nextInt(1, 4);
                    double occupancy = Math.round(ThreadLocalRandom.current().nextDouble(0, 100) * 100.0) / 100.0;
                    return new WorkspaceReading(workspaceId, occupancy, LocalDate.now(), LocalDateTime.now());
                })
                .doOnNext(repository::save)
                .then(Mono.just("Generación finalizada: 10 lecturas emitidas al stream"));
    }
}