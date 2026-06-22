package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.repository.WorkspaceReadingRepository;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Puente entre el mundo MVC (reservas, JPA, base de datos) y el módulo reactivo.
 * Calcula la ocupación REAL de una sala a partir de sus reservas confirmadas en curso
 * y la emite al stream reactivo. Lo llama BookingServiceImpl cuando se confirma/cancela una reserva.
 */
@Component
public class OccupancyPublisher {

    private final WorkspaceReadingRepository readingRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BookingRepository bookingRepository;

    public OccupancyPublisher(WorkspaceReadingRepository readingRepository,
                              WorkspaceRepository workspaceRepository,
                              BookingRepository bookingRepository) {
        this.readingRepository = readingRepository;
        this.workspaceRepository = workspaceRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Calcula la ocupación real de UNA sala (reservas CONFIRMADAS en curso ahora ÷ capacidad)
     * y la emite al stream. Devuelve la lectura emitida (o null si la sala no existe).
     */
    public WorkspaceReading publishForWorkspace(Long workspaceId) {
        Workspace ws = workspaceRepository.findById(workspaceId).orElse(null);
        if (ws == null) return null;

        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Guayaquil"));

        int enCurso = bookingRepository
                .findByWorkspaceIdAndStatusAndStartDatetimeLessThanEqualAndEndDatetimeGreaterThanEqual(
                        workspaceId, BookingStatus.CONFIRMADA, now, now)
                .size();

        double capacidad = (ws.getCapacity() != null && ws.getCapacity() > 0) ? ws.getCapacity() : 1;
        double ocupacion = Math.min(100.0, (enCurso / capacidad) * 100.0);
        ocupacion = Math.round(ocupacion * 100.0) / 100.0;

        // save() guarda en memoria y emite al stream reactivo (Sinks)
        return readingRepository.save(new WorkspaceReading(ws.getName(), ocupacion, now));
    }

    /**
     * Calcula y emite la ocupación de TODAS las salas. Se usa para tomar una "foto"
     * del estado actual (por ejemplo al abrir el monitor por primera vez).
     */
    public List<WorkspaceReading> publishAll() {
        return workspaceRepository.findAll().stream()
                .map(ws -> publishForWorkspace(ws.getId()))
                .filter(r -> r != null)
                .toList();
    }
}