package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.domain.Booking;
import ec.edu.espe.coworkingapp.reactive.model.PaymentTransaction;
import ec.edu.espe.coworkingapp.reactive.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Puente entre el flujo MVC (reservas) y el módulo reactivo de transacciones.
 * Cuando se confirma una reserva se crea su transacción de pago (ACTIVA) y se emite al stream.
 * Cuando se cancela/elimina, la transacción pasa a CANCELADA y se emite el cambio.
 */
@Component
public class PaymentPublisher {

    private final PaymentTransactionRepository repository;

    public PaymentPublisher(PaymentTransactionRepository repository) {
        this.repository = repository;
    }

    public void registerForBooking(Booking b) {
        double hours = b.getTotalHours() != null ? b.getTotalHours() : 0.0;
        double price = hours * b.getWorkspace().getPricePerHour();
        double amount = Math.round(price * 100.0) / 100.0;

        repository.save(new PaymentTransaction(
                null,
                b.getId(),
                b.getWorkspace().getName(),
                b.getMember().getFullName(),
                amount,
                "ACTIVA",
                LocalDateTime.now()));
    }

    public void cancelForBooking(Long bookingId) {
        PaymentTransaction tx = repository.findActiveByBookingId(bookingId);
        if (tx != null) repository.cancel(tx);
    }
}
