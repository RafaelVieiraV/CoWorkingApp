package ec.edu.espe.coworkingapp.reactive.controller;

import ec.edu.espe.coworkingapp.reactive.model.PaymentTransaction;
import ec.edu.espe.coworkingapp.reactive.service.PaymentService;
import ec.edu.espe.coworkingapp.service.BookingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    // PASO 2: registrar un pago manualmente (para pruebas del lab en Postman)
    // POST /api/payments  Body: { "workspaceName":"Sala A", "memberName":"Ana", "amount":12.5 }
    @PostMapping
    public Mono<PaymentTransaction> register(@RequestBody PaymentTransaction tx) {
        tx.setId(null);
        if (tx.getStatus() == null) tx.setStatus("ACTIVA");
        tx.setTimestamp(LocalDateTime.now());
        return paymentService.register(tx);
    }

    // PASO 1 y 3: listar todas las transacciones (devuelve [] si no hay)
    @GetMapping
    public Flux<PaymentTransaction> getAll() {
        return paymentService.getAll();
    }

    // Reservas activas (pagos activos)
    @GetMapping("/active")
    public Flux<PaymentTransaction> getActive() {
        return paymentService.getActive();
    }

    // PASO 4 y 5: promedio asíncrono (~3s) y NO bloqueante (delayElement)
    @GetMapping("/average")
    public Mono<Double> average() {
        return paymentService.averageAmount();
    }

    // PASO 6: stream en tiempo real con SSE
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PaymentTransaction> stream() {
        return paymentService.getStream();
    }

    // Cancelar el pago: si viene de una reserva, cancela la reserva (y su pago); si no, cancela solo el pago
    @PostMapping("/{id}/cancel")
    public Mono<PaymentTransaction> cancel(@PathVariable Long id) {
        PaymentTransaction tx = paymentService.findById(id);
        if (tx == null) return Mono.empty();

        if (tx.getBookingId() != null) {
            try {
                bookingService.cancel(tx.getBookingId()); // cancela la reserva; el hook marca la transacción
            } catch (Exception e) {
                paymentService.markCancelled(id);          // si la reserva ya no se puede cancelar, marca el pago
            }
        } else {
            paymentService.markCancelled(id);
        }
        return Mono.just(paymentService.findById(id));
    }
}
