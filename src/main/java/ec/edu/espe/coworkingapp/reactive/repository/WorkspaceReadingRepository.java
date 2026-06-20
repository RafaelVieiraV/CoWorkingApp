package ec.edu.espe.coworkingapp.reactive.repository;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorkspaceReadingRepository {

    // Lista concurrente para guardar las lecturas
    private final List<WorkspaceReading> readings = new CopyOnWriteArrayList<>();

    // Fuente de datos reactivos (multicast = varios suscriptores)
    private final Sinks.Many<WorkspaceReading> sink = Sinks.many().multicast().onBackpressureBuffer();

    // Guarda una lectura y la emite al stream
    public WorkspaceReading save(WorkspaceReading reading) {
        readings.add(reading);
        sink.tryEmitNext(reading);
        return reading;
    }

    // Devuelve todas las lecturas como Flux
    public Flux<WorkspaceReading> findAll() {
        return Flux.fromIterable(readings);
    }

    // Devuelve el stream en vivo de lecturas
    public Flux<WorkspaceReading> getLiveStream() {
        return sink.asFlux();
    }
}