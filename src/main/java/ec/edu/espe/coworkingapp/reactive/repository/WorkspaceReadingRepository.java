package ec.edu.espe.coworkingapp.reactive.repository;

import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorkspaceReadingRepository {

    // Lista concurrente para guardar TODAS las lecturas (persisten mientras la app esté viva)
    private final List<WorkspaceReading> readings = new CopyOnWriteArrayList<>();

    // Fuente de datos reactivos (multicast = varios suscriptores reciben lo mismo)
    private final Sinks.Many<WorkspaceReading> sink = Sinks.many().multicast().onBackpressureBuffer();

    // Guarda una lectura y la emite al stream en vivo
    public WorkspaceReading save(WorkspaceReading reading) {
        readings.add(reading);
        sink.tryEmitNext(reading);
        return reading;
    }

    // Devuelve TODAS las lecturas como Flux
    public Flux<WorkspaceReading> findAll() {
        return Flux.fromIterable(readings);
    }

    // Devuelve el stream "en vivo" (hot stream): cada nueva lectura se emite a los suscriptores
    public Flux<WorkspaceReading> getLiveStream() {
        return sink.asFlux();
    }
}