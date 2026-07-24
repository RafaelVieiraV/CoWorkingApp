package ec.edu.espe.coworkingapp.wallet.repository;

import ec.edu.espe.coworkingapp.wallet.model.Wallet;
import java.util.Optional;

// Interfaz del repositorio — dependencia a mockear
public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(String id);
    boolean existsByOwnerEmail(String email);
}