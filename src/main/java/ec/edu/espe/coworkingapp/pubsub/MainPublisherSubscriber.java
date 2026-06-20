package ec.edu.espe.coworkingapp.pubsub;

import reactor.core.publisher.Flux;

public class MainPublisherSubscriber {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== PARTE 2: Publisher y Subscriber básico ===");

        // Crear un flujo que envía 10 valores
        Flux<Integer> flux = Flux.range(1, 10)
                .map(i -> i * 2);

        // Suscribirse con las tres etapas del ciclo de vida
        flux.subscribe(
                value -> System.out.println("onNext: " + value),
                error -> System.err.println("onError: " + error.getMessage()),
                () -> System.out.println("onComplete: flujo terminado")
        );

        System.out.println("\n=== PARTE 5: Mini sistema de reservas (Publisher-Subscriber) ===");

        // Emitir montos de reservas del coworking
        Flux<Double> bookingAmounts = Flux.just(2.50, 5.00, 12.00, 1.00, 25.00, 8.50);

        bookingAmounts
                // Filtrar reservas >= 5.00
                .filter(amount -> amount >= 5.00)
                // Aplicar IVA del 12%
                .map(amount -> amount * 1.12)
                // Generar error si el monto supera 20.00
                .map(amount -> {
                    if (amount > 20.00) {
                        throw new RuntimeException("Monto inválido: " + amount);
                    }
                    return amount;
                })
                // Recuperar error con valores por defecto
                .onErrorResume(e -> {
                    System.out.println("onError: " + e.getMessage());
                    return Flux.just(6.00, 7.00, 8.00);
                })
                .subscribe(
                        value -> System.out.printf("onNext: $%.2f%n", value),
                        error -> System.err.println("onError: " + error.getMessage()),
                        () -> System.out.println("onComplete: procesamiento de reservas terminado")
                );
    }
}