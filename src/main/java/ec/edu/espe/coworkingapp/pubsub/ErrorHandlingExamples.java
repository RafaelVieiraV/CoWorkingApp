package ec.edu.espe.coworkingapp.pubsub;

import reactor.core.publisher.Flux;

public class ErrorHandlingExamples {

    public static void runExamples() {

        System.out.println("\n=== PARTE 4: Manejo de errores ===");

        // Paso 1: Error sin recuperación
        System.out.println("--- Error sin recuperación ---");
        Flux.range(1, 10)
                .map(n -> {
                    if (n == 5) throw new RuntimeException("Error en valor 5");
                    return n;
                })
                .subscribe(
                        value -> System.out.println("onNext: " + value),
                        error -> System.err.println("onError: " + error.getMessage()),
                        () -> System.out.println("onComplete")
                );

        // Paso 2: Error con recuperación
        System.out.println("\n--- Error con recuperación ---");
        Flux.range(1, 10)
                .map(n -> {
                    if (n == 5) throw new RuntimeException("Error en valor 5");
                    return n;
                })
                .onErrorResume(e -> {
                    System.out.println("Recuperando del error: " + e.getMessage());
                    return Flux.just(10, 20, 30);
                })
                .subscribe(
                        value -> System.out.println("onNext: " + value),
                        error -> System.err.println("onError: " + error.getMessage()),
                        () -> System.out.println("onComplete: flujo recuperado")
                );
    }
}