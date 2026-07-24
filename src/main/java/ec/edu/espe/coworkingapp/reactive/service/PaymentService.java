package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.reactive.model.PaymentView;
import ec.edu.espe.coworkingapp.service.BookingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final BookingService bookingService;

    // Estado de pago por reserva (en memoria): bookingId -> PENDIENTE | PAGADO
    private final Map<Long, String> paymentStatus = new ConcurrentHashMap<>();

    // Fuente reactiva multicast para emitir cambios al stream
    private final Sinks.Many<PaymentView> sink = Sinks.many().multicast().directBestEffort();

    public PaymentService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private PaymentView toView(BookingResponseDto b, String status) {
        return new PaymentView(b.getId(), b.getWorkspaceName(), b.getMemberFullName(),
                b.getTotalPrice(), status, LocalDateTime.now());
    }

    // Reservas activas = reservas CONFIRMADAS reales, con su estado de pago. (Flux creado directo)
    public Flux<PaymentView> getActive() {
        return Flux.fromIterable(bookingService.findByStatus(BookingStatus.CONFIRMADA))
                .map(b -> toView(b, paymentStatus.getOrDefault(b.getId(), "PENDIENTE")));
    }

    // Todas las reservas (lab: listar todas)
    public Flux<PaymentView> getAll() {
        return Flux.fromIterable(bookingService.findAll())
                .map(b -> {
                    String status = b.getStatus() == BookingStatus.CANCELADA
                            ? "CANCELADA"
                            : paymentStatus.getOrDefault(b.getId(), "PENDIENTE");
                    return toView(b, status);
                });
    }

    // Registrar el pago de una reserva -> Mono creado directo
    public Mono<PaymentView> pay(Long bookingId) {
        BookingResponseDto b = bookingService.findById(bookingId);
        paymentStatus.put(bookingId, "PAGADO");
        PaymentView v = toView(b, "PAGADO");
        sink.tryEmitNext(v);
        return Mono.just(v);
    }

    // Cancelar: cancela la reserva real y emite el cambio
    public Mono<PaymentView> cancel(Long bookingId) {
        bookingService.cancel(bookingId);
        paymentStatus.remove(bookingId);
        BookingResponseDto b = bookingService.findById(bookingId);
        PaymentView v = toView(b, "CANCELADA");
        sink.tryEmitNext(v);
        return Mono.just(v);
    }

    // Promedio del monto de las reservas activas. Retraso de 3s NATIVO (delayElement), sin Thread.sleep.
    public Mono<Double> averageAmount() {
        return Flux.fromIterable(bookingService.findByStatus(BookingStatus.CONFIRMADA))
                .map(BookingResponseDto::getTotalPrice)
                .collectList()
                .map(list -> list.isEmpty()
                        ? 0.0
                        : list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                .delayElement(Duration.ofSeconds(3));
    }

    // Stream en vivo
    public Flux<PaymentView> stream() {
        return sink.asFlux();
    }
}