package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.repository.WorkspaceReadingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class WorkspaceMonitorService {

    private final WorkspaceReadingRepository repository;

    public WorkspaceMonitorService(WorkspaceReadingRepository repository) {
        this.repository = repository;
    }

    // Guarda una lectura y devuelve un Mono con la lectura guardada
    public Mono<WorkspaceReading> saveReading(WorkspaceReading reading) {
        return Mono.fromCallable(() -> repository.save(reading))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Devuelve todas las lecturas como Flux
    public Flux<WorkspaceReading> getAllReadings() {
        return repository.findAll();
    }

    // Devuelve el stream en tiempo real
    public Flux<WorkspaceReading> getLiveStream() {
        return repository.getLiveStream();
    }

    // Calcula el promedio de ocupación de forma asíncrona
    public Mono<Double> getAverageOccupancy() {
        return repository.findAll()
                .map(WorkspaceReading::getOccupancyPercentage)
                .collectList()
                .flatMap(list -> Mono.fromCallable(() -> {
                    Thread.sleep(3000); // Simula operación costosa
                    return list.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}