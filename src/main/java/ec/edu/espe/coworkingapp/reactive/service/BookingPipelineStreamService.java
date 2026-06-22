package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.service.BookingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BookingPipelineStreamService {

    private static final double HORAS_MAXIMAS_NORMALES = 12.0;
    private static final int TAMANO_LOTE = 2; // backpressure: request(2)

    private final BookingService bookingService;

    public BookingPipelineStreamService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Devuelve un Flux<String> que emite los eventos del pipeline en tiempo real
     * Este Flux se sirve directamente como SSE por PaymentController.
     */
    public Flux<String> ejecutarYEmitirEventos() {
        List<BookingResponseDto> reservasReales = bookingService.findByStatus(BookingStatus.CONFIRMADA);

        // Sink multicast para empujar cada evento del pipeline como mensaje SSE
        Sinks.Many<String> eventos = Sinks.many().unicast().onBackpressureBuffer();

        if (reservasReales.isEmpty()) {
            return Flux.just("[info] No hay reservas en la base de datos. Crea reservas antes de ejecutar el pipeline.");
        }

        AtomicInteger ultimaPosicionEvaluada = new AtomicInteger(-1);
        AtomicInteger procesadosEnLote = new AtomicInteger(0);

        // emitir onSubscribe de inmediato
        eventos.tryEmitNext("[onSubscribe] Pipeline iniciado. " + reservasReales.size()
                + " reserva(s) real(es) encontrada(s) en la BD. Solicitando lote de " + TAMANO_LOTE + "...");

        Flux<BookingResponseDto> flujoPrincipal = Flux.fromIterable(reservasReales)
                .index()
                .map(tupla -> {
                    long indice = tupla.getT1();
                    BookingResponseDto reserva = tupla.getT2();
                    ultimaPosicionEvaluada.set((int) indice);

                    Double horas = reserva.getTotalHours();
                    if (horas != null && horas > HORAS_MAXIMAS_NORMALES) {
                        throw new IllegalStateException(
                                "Reserva fuera de lo normal: bookingId=" + reserva.getId()
                                        + ", totalHours=" + horas
                                        + " (máximo permitido: " + HORAS_MAXIMAS_NORMALES + "h)");
                    }
                    return reserva;
                })
                .delayElements(Duration.ofMillis(400))
                .doOnNext(reserva -> {
                    int loteActual = procesadosEnLote.incrementAndGet();
                    eventos.tryEmitNext("[onNext] bookingId=" + reserva.getId()
                            + " | " + reserva.getMemberFullName()
                            + " | " + reserva.getWorkspaceName()
                            + " | " + reserva.getTotalHours() + "h"
                            + " | $" + String.format("%.2f", reserva.getTotalPrice()));

                    if (loteActual % TAMANO_LOTE == 0) {
                        eventos.tryEmitNext("[backpressure] Lote de " + TAMANO_LOTE
                                + " completado. Solicitando " + TAMANO_LOTE + " más...");
                    }
                })
                .onErrorResume(error -> {
                    eventos.tryEmitNext("[onError] " + error.getMessage());
                    int desde = ultimaPosicionEvaluada.get() + 1;

                    if (desde >= reservasReales.size()) {
                        eventos.tryEmitNext("[onErrorResume] No quedan más reservas por verificar.");
                        eventos.tryEmitComplete();
                        return Flux.empty();
                    }

                    List<BookingResponseDto> restantes = reservasReales.subList(desde, reservasReales.size());
                    eventos.tryEmitNext("[onErrorResume] Recuperando flujo real con "
                            + restantes.size() + " reserva(s) restante(s) de la BD...");

                    return Flux.fromIterable(restantes)
                            .delayElements(Duration.ofMillis(400))
                            .doOnNext(r -> {
                                int lote = procesadosEnLote.incrementAndGet();
                                eventos.tryEmitNext("[onNext] bookingId=" + r.getId()
                                        + " | " + r.getMemberFullName()
                                        + " | " + r.getWorkspaceName()
                                        + " | " + r.getTotalHours() + "h"
                                        + " | $" + String.format("%.2f", r.getTotalPrice()));
                                if (lote % TAMANO_LOTE == 0) {
                                    eventos.tryEmitNext("[backpressure] Lote de " + TAMANO_LOTE
                                            + " completado. Solicitando " + TAMANO_LOTE + " más...");
                                }
                            });
                });

        // Suscribirse al flujo: los eventos van al sink, el sink va al SSE
        flujoPrincipal.subscribe(
                v -> {}, // onNext ya capturado por doOnNext
                err -> {
                    eventos.tryEmitNext("[onError] Error no recuperado: " + err.getMessage());
                    eventos.tryEmitComplete();
                },
                () -> {
                    eventos.tryEmitNext("[onComplete] Verificación finalizada. "
                            + procesadosEnLote.get() + " reserva(s) procesada(s).");
                    eventos.tryEmitComplete();
                }
        );

        return eventos.asFlux();
    }
}