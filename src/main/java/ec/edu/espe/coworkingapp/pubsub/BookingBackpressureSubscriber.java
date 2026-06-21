package ec.edu.espe.coworkingapp.pubsub;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 consume el flujo de reservas (BookingResponseDto) aplicando
 backpressure manual: solicita las reservas de a 2 en 2 con
 subscription.request(2).
 Solo recibe lo que el Publisher le entrega; no genera ni transforma
 */
public class BookingBackpressureSubscriber implements Subscriber<BookingResponseDto> {

    private static final int TAMANO_LOTE = 2; // backpressure: request(2)

    private Subscription subscription;
    private int procesadosEnEsteLote = 0;
    private boolean huboError = false;
    private String mensajeError;
    private final List<BookingResponseDto> resultados = new ArrayList<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        System.out.println("[onSubscribe] Iniciando verificación de reservas (lotes de " + TAMANO_LOTE + ")");
        subscription.request(TAMANO_LOTE);
    }

    @Override
    public void onNext(BookingResponseDto reserva) {
        System.out.println("[onNext] bookingId=" + reserva.getId()
                + " miembro=" + reserva.getMemberFullName()
                + " espacio=" + reserva.getWorkspaceName()
                + " totalHours=" + reserva.getTotalHours()
                + " totalPrice=$" + reserva.getTotalPrice());
        resultados.add(reserva);
        procesadosEnEsteLote++;

        if (procesadosEnEsteLote == TAMANO_LOTE) {
            procesadosEnEsteLote = 0;
            System.out.println("[backpressure] Lote completado. Solicitando " + TAMANO_LOTE + " reservas más...");
            subscription.request(TAMANO_LOTE);
        }
    }

    @Override
    public void onError(Throwable t) {
        huboError = true;
        mensajeError = t.getMessage();
        System.out.println("[onError] " + t.getMessage());
        latch.countDown();
    }

    @Override
    public void onComplete() {
        System.out.println("[onComplete] Verificación de reservas finalizada. Total procesadas: " + resultados.size());
        latch.countDown();
    }

    public void esperarFinalizacion() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<BookingResponseDto> getResultados() {
        return resultados;
    }

    public boolean isHuboError() {
        return huboError;
    }

    public String getMensajeError() {
        return mensajeError;
    }
}