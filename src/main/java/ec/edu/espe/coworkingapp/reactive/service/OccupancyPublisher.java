package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.reactive.model.WorkspaceReading;
import ec.edu.espe.coworkingapp.reactive.repository.WorkspaceReadingRepository;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Puente entre el flujo MVC (reservas, JPA) y el módulo reactivo.
 * Calcula la ocupación REAL de una sala para un DÍA (reservas confirmadas de ese día ÷ capacidad)
 * y la emite al stream. Lo llama BookingServiceImpl al confirmar, cancelar o eliminar una reserva.
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
     * Calcula la ocupación de una sala para un día concreto y la emite al stream.
     * Ocupación = (reservas CONFIRMADAS de ese día ÷ capacidad) × 100.
     */
    public WorkspaceReading publishForWorkspaceDay(Long workspaceId, LocalDate day) {
        Workspace ws = workspaceRepository.findById(workspaceId).orElse(null);
        if (ws == null) return null;

        LocalDateTime inicioDia = day.atStartOfDay();
        LocalDateTime finDia = day.atTime(LocalTime.MAX);

        int reservasDelDia = bookingRepository
                .findByWorkspaceIdAndStatusAndStartDatetimeBetween(
                        workspaceId, BookingStatus.CONFIRMADA, inicioDia, finDia)
                .size();

        double capacidad = (ws.getCapacity() != null && ws.getCapacity() > 0) ? ws.getCapacity() : 1;
        double ocupacion = Math.min(100.0, (reservasDelDia / capacidad) * 100.0);
        ocupacion = Math.round(ocupacion * 100.0) / 100.0;

        // save() guarda en memoria y emite al stream reactivo (Sinks)
        return readingRepository.save(new WorkspaceReading(ws.getName(), ocupacion, day, LocalDateTime.now()));
    }
}