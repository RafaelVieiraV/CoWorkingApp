package ec.edu.espe.coworkingapp.reactive.repository;

import ec.edu.espe.coworkingapp.reactive.model.PaymentTransaction;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PaymentTransactionRepository {

    // Almacén en memoria
    private final List<PaymentTransaction> transactions = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0);

    // Fuente reactiva multicast (varios suscriptores reciben lo mismo)
    private final Sinks.Many<PaymentTransaction> sink = Sinks.many().multicast().onBackpressureBuffer();

    // Guarda una transacción y la emite al stream
    public PaymentTransaction save(PaymentTransaction tx) {
        if (tx.getId() == null) tx.setId(sequence.incrementAndGet());
        transactions.add(tx);
        sink.tryEmitNext(tx);
        return tx;
    }

    // Todas las transacciones (Flux creado directo)
    public Flux<PaymentTransaction> findAll() {
        return Flux.fromIterable(transactions);
    }

    // Solo las activas (Flux creado directo)
    public Flux<PaymentTransaction> findActive() {
        return Flux.fromIterable(transactions).filter(t -> "ACTIVA".equals(t.getStatus()));
    }

    public PaymentTransaction findById(Long id) {
        return transactions.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
    }

    public PaymentTransaction findActiveByBookingId(Long bookingId) {
        return transactions.stream()
                .filter(t -> bookingId.equals(t.getBookingId()) && "ACTIVA".equals(t.getStatus()))
                .findFirst().orElse(null);
    }

    // Marca una transacción como cancelada y re-emite el cambio al stream
    public void cancel(PaymentTransaction tx) {
        tx.setStatus("CANCELADA");
        tx.setTimestamp(LocalDateTime.now());
        sink.tryEmitNext(tx);
    }

    // Stream en vivo
    public Flux<PaymentTransaction> getLiveStream() {
        return sink.asFlux();
    }
}
