package ec.edu.espe.coworkingapp.pubsub;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("/api/pubsub")
public class BookingProcessorController {

    // Endpoint 1: Publisher básico — emite 10 valores
    @GetMapping("/publisher")
    public Flux<Integer> basicPublisher() {
        return Flux.range(1, 10)
                .map(i -> i * 2);
    }

    // Endpoint 2: Procesar montos de reservas con filtro, IVA y manejo de errores
    @GetMapping("/bookings/process")
    public Flux<Double> processBookings() {
        return Flux.just(2.50, 5.00, 12.00, 1.00, 25.00, 8.50)
                // Filtrar montos >= 5.00
                .filter(amount -> amount >= 5.00)
                // Aplicar IVA del 12%
                .map(amount -> amount * 1.12)
                // Error si supera 20.00
                .map(amount -> {
                    if (amount > 20.00) {
                        throw new RuntimeException("Monto inválido: " + amount);
                    }
                    return amount;
                })
                // Recuperar con valores por defecto
                .onErrorResume(e -> Flux.just(6.00, 7.00, 8.00));
    }

    // Endpoint 3: Flujo asíncrono con backpressure (un valor cada 500ms)
    @GetMapping(value = "/bookings/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Double> bookingStream() {
        return Flux.just(2.50, 5.00, 12.00, 1.00, 8.50)
                .filter(amount -> amount >= 5.00)
                .map(amount -> amount * 1.12)
                .delayElements(Duration.ofMillis(500));
    }

    // Endpoint 4: Promedio de montos de forma asíncrona
    @GetMapping("/bookings/average")
    public Mono<Double> averageBookings() {
        return Flux.just(5.00, 12.00, 8.50)
                .map(amount -> amount * 1.12)
                .collectList()
                .flatMap(list -> Mono.fromCallable(() -> {
                    Thread.sleep(2000); // Simula operación asíncrona
                    return list.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    // Endpoint 5: Manejo de errores
    @GetMapping("/errors/demo")
    public Flux<Integer> errorDemo() {
        return Flux.range(1, 10)
                .map(n -> {
                    if (n == 5) throw new RuntimeException("Error en valor 5");
                    return n;
                })
                .onErrorResume(e -> Flux.just(10, 20, 30));
    }
}