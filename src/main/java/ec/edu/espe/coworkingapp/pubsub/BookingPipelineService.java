package ec.edu.espe.coworkingapp.pubsub;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.service.BookingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 Pipeline reactivo (Project Reactor) se ejecuta automáticamente cada vez que se crea una reserva nueva
 una reserva de más de 12 horas seguidas
 */
@Service
public class BookingPipelineService {

    private static final double HORAS_MAXIMAS_NORMALES = 12.0;

    private final BookingService bookingService;

    public BookingPipelineService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    //Corre el pipeline reactivo sobre TODAS las reservas reales actuales
    public BookingPipelineResult verificarReservasReales() {
        List<BookingResponseDto> reservasReales = bookingService.findAll();

        if (reservasReales.isEmpty()) {
            return new BookingPipelineResult(0, List.of(), false, null);
        }

        // Para que la recuperación retome el flujo real
        AtomicInteger ultimaPosicionEvaluada = new AtomicInteger(-1);

        Flux<BookingResponseDto> flujo = Flux.fromIterable(reservasReales)
                .index() // adjunta la posición real (0,1,2,...) de cada reserva
                .map(tupla -> {
                    long indice = tupla.getT1();
                    BookingResponseDto reserva = tupla.getT2();
                    ultimaPosicionEvaluada.set((int) indice);

                    Double horas = reserva.getTotalHours();
                    if (horas != null && horas > HORAS_MAXIMAS_NORMALES) {
                        throw new IllegalStateException(
                                "Reserva fuera de lo normal: bookingId=" + reserva.getId()
                                        + ", totalHours=" + horas
                                        + " supera el máximo permitido de " + HORAS_MAXIMAS_NORMALES + " horas.");
                    }
                    return reserva;
                })
                // simula verificación asíncrona en vivo, una reserva real cada 300ms
                .delayElements(Duration.ofMillis(300))
                .onErrorResume(error -> {
                    System.out.println("[onErrorResume] " + error.getMessage());
                    int desde = ultimaPosicionEvaluada.get() + 1;
                    if (desde >= reservasReales.size()) {
                        System.out.println("[onErrorResume] No quedan más reservas reales por verificar.");
                        return Flux.empty();
                    }
                    List<BookingResponseDto> restantes = reservasReales.subList(desde, reservasReales.size());
                    System.out.println("[onErrorResume] Reanudando verificación con las " + restantes.size()
                            + " reserva(s) real(es) restante(s).");
                    return Flux.fromIterable(restantes)
                            .delayElements(Duration.ofMillis(300));
                });

        BookingBackpressureSubscriber subscriber = new BookingBackpressureSubscriber();
        flujo.subscribe(subscriber);
        subscriber.esperarFinalizacion();

        return new BookingPipelineResult(
                reservasReales.size(),
                subscriber.getResultados(),
                subscriber.isHuboError(),
                subscriber.getMensajeError()
        );
    }
}