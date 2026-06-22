package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.domain.Booking;
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
import java.util.List;

/**
 * Puente entre el flujo MVC (reservas, JPA) y el módulo reactivo.
 * Para una sala y un día calcula:
 *   - cantidad de reservas confirmadas de ese día
 *   - % de ocupación = horas reservadas ÷ horas de la jornada operativa (8:00–20:00 = 12h)
 * y emite la lectura al stream reactivo.
 */
@Component
public class OccupancyPublisher {

    // Jornada operativa del coworking en horas (8:00 a 20:00). Sirve de base para el %.
    private static final double HORAS_JORNADA = 12.0;

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

    public WorkspaceReading publishForWorkspaceDay(Long workspaceId, LocalDate day) {
        Workspace ws = workspaceRepository.findById(workspaceId).orElse(null);
        if (ws == null) return null;

        LocalDateTime inicioDia = day.atStartOfDay();
        LocalDateTime finDia = day.atTime(LocalTime.MAX);

        List<Booking> reservas = bookingRepository
                .findByWorkspaceIdAndStatusAndStartDatetimeBetween(
                        workspaceId, BookingStatus.CONFIRMADA, inicioDia, finDia);

        int cantidad = reservas.size();

        // % = horas reservadas ese día ÷ horas de la jornada (tope 100%)
        double horasReservadas = reservas.stream()
                .mapToDouble(b -> b.getTotalHours() != null ? b.getTotalHours() : 0.0)
                .sum();
        double ocupacion = Math.min(100.0, (horasReservadas / HORAS_JORNADA) * 100.0);
        ocupacion = Math.round(ocupacion * 100.0) / 100.0;

        // save() guarda en memoria y emite al stream reactivo (Sinks)
        return readingRepository.save(
                new WorkspaceReading(ws.getName(), cantidad, ocupacion, day, LocalDateTime.now()));
    }
}