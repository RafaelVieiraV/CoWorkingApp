package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.reactive.model.PaymentTransaction;
import ec.edu.espe.coworkingapp.reactive.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PaymentService {

    private final PaymentTransactionRepository repository;

    public PaymentService(PaymentTransactionRepository repository) {
        this.repository = repository;
    }

    // Registrar un pago -> Mono creado directo con Mono.just
    public Mono<PaymentTransaction> register(PaymentTransaction tx) {
        return Mono.just(repository.save(tx));
    }

    // Todas las transacciones (Flux directo)
    public Flux<PaymentTransaction> getAll() {
        return repository.findAll();
    }

    // Reservas/pagos activos (Flux directo)
    public Flux<PaymentTransaction> getActive() {
        return repository.findActive();
    }

    // Stream en vivo (Flux directo)
    public Flux<PaymentTransaction> getStream() {
        return repository.getLiveStream();
    }

    public PaymentTransaction findById(Long id) {
        return repository.findById(id);
    }

    public void markCancelled(Long id) {
        PaymentTransaction tx = repository.findById(id);
        if (tx != null) repository.cancel(tx);
    }

    /**
     * Promedio del monto de los pagos ACTIVOS.
     * El retraso de 3s es 100% reactivo (delayElement): NO usa Thread.sleep, NO bloquea ningún hilo.
     */
    public Mono<Double> averageAmount() {
        return repository.findActive()
                .map(PaymentTransaction::getAmount)
                .collectList()
                .map(list -> list.isEmpty()
                        ? 0.0
                        : list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                .delayElement(Duration.ofSeconds(3));
    }
}
