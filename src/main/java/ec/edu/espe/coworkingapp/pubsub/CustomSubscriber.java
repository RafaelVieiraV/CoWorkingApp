package ec.edu.espe.coworkingapp.pubsub;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

public class CustomSubscriber extends BaseSubscriber<Integer> {

    private int count = 0;

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        System.out.println("Suscrito - solicitando 2 elementos");
        // Backpressure: solicitar solo 2 elementos a la vez
        request(2);
    }

    @Override
    protected void hookOnNext(Integer value) {
        count++;
        System.out.println("onNext: " + value);
        // Cada 2 elementos, solicitar 2 más
        if (count % 2 == 0) {
            System.out.println("Solicitando 2 más...");
            request(2);
        }
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        System.err.println("onError: " + throwable.getMessage());
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("onComplete: subscriber personalizado terminado");
    }
}