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
    private final ec.edu.espe.coworkingapp.repository.WorkspaceRepository workspaceRepository;
    private final ec.edu.espe.coworkingapp.repository.BookingRepository bookingRepository;


    public WorkspaceMonitorService(WorkspaceReadingRepository repository,
                                   ec.edu.espe.coworkingapp.repository.WorkspaceRepository workspaceRepository,
                                   ec.edu.espe.coworkingapp.repository.BookingRepository bookingRepository) {
        this.repository = repository;
        this.workspaceRepository = workspaceRepository;
        this.bookingRepository = bookingRepository;
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

    // Calcula la ocupación REAL de cada sala desde las reservas confirmadas activas ahora
    public reactor.core.publisher.Flux<WorkspaceReading> refreshRealOccupancy() {
        return reactor.core.publisher.Mono.fromCallable(() -> {
                    java.time.LocalDateTime now =
                            java.time.LocalDateTime.now(java.time.ZoneId.of("America/Guayaquil"));

                    return workspaceRepository.findAll().stream().map(ws -> {
                        int activos = bookingRepository
                                .findByWorkspaceIdAndStatusAndStartDatetimeLessThanEqualAndEndDatetimeGreaterThanEqual(
                                        ws.getId(),
                                        ec.edu.espe.coworkingapp.domain.BookingStatus.CONFIRMADA,
                                        now, now)
                                .size();

                        double capacidad = ws.getCapacity() != null && ws.getCapacity() > 0 ? ws.getCapacity() : 1;
                        double ocupacion = Math.min(100.0, (activos / capacidad) * 100.0);
                        ocupacion = Math.round(ocupacion * 100.0) / 100.0;

                        return new WorkspaceReading(ws.getName(), ocupacion, now);
                    }).toList();
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .flatMapMany(reactor.core.publisher.Flux::fromIterable)
                .doOnNext(repository::save); // guarda y emite cada lectura al stream en vivo
    }
}