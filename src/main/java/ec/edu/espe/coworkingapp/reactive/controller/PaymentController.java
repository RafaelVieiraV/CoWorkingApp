package ec.edu.espe.coworkingapp.reactive.controller;

import ec.edu.espe.coworkingapp.reactive.model.PaymentView;
import ec.edu.espe.coworkingapp.reactive.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // PASO 1 y 3: listar todas las reservas (devuelve [] si no hay)
    @GetMapping
    public Flux<PaymentView> getAll() {
        return paymentService.getAll();
    }

    // Reservas activas (confirmadas) con su estado de pago
    @GetMapping("/active")
    public Flux<PaymentView> getActive() {
        return paymentService.getActive();
    }

    // PASO 2: registrar el pago de una reserva (POST)
    @PostMapping("/{bookingId}/pay")
    public Mono<PaymentView> pay(@PathVariable Long bookingId) {
        return paymentService.pay(bookingId);
    }

    // Cancelar la reserva (y su pago)
    @PostMapping("/{bookingId}/cancel")
    public Mono<PaymentView> cancel(@PathVariable Long bookingId) {
        return paymentService.cancel(bookingId);
    }

    // PASO 4 y 5: promedio asíncrono (~3s) y NO bloqueante (delayElement)
    @GetMapping("/average")
    public Mono<Double> average() {
        return paymentService.averageAmount();
    }

    // PASO 6: stream en tiempo real con SSE
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PaymentView> stream() {
        return paymentService.stream();
    }
}