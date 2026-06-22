package ec.edu.espe.coworkingapp.reactive.controller;

import ec.edu.espe.coworkingapp.reactive.model.PaymentView;
import ec.edu.espe.coworkingapp.reactive.service.PaymentService;
import ec.edu.espe.coworkingapp.reactive.service.BookingPipelineStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingPipelineStreamService pipelineStreamService;

    public PaymentController(PaymentService paymentService,
                             BookingPipelineStreamService pipelineStreamService) {
        this.paymentService = paymentService;
        this.pipelineStreamService = pipelineStreamService;
    }

    @GetMapping
    public Flux<PaymentView> getAll() {
        return paymentService.getAll();
    }

    @GetMapping("/active")
    public Flux<PaymentView> getActive() {
        return paymentService.getActive();
    }

    @PostMapping("/{bookingId}/pay")
    public Mono<PaymentView> pay(@PathVariable Long bookingId) {
        return paymentService.pay(bookingId);
    }

    @PostMapping("/{bookingId}/cancel")
    public Mono<PaymentView> cancel(@PathVariable Long bookingId) {
        return paymentService.cancel(bookingId);
    }

    @GetMapping("/average")
    public Mono<Double> average() {
        return paymentService.averageAmount();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PaymentView> stream() {
        return paymentService.stream();
    }


     //SSE: emite los eventos del pipeline de verificación de reservas en tiempo real
    @GetMapping(value = "/pipeline-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> pipelineStream() {
        return pipelineStreamService.ejecutarYEmitirEventos();
    }
}