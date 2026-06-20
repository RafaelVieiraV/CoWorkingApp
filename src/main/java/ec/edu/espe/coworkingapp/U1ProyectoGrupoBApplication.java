package ec.edu.espe.coworkingapp;

import ec.edu.espe.coworkingapp.reactive.repository.WorkspaceReadingRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class U1ProyectoGrupoBApplication {

    public static void main(String[] args) {
        SpringApplication.run(U1ProyectoGrupoBApplication.class, args);
    }

    // Registramos el repositorio reactivo como Bean
    @Bean
    public WorkspaceReadingRepository workspaceReadingRepository() {
        return new WorkspaceReadingRepository();
    }
}