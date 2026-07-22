package ec.edu.espe.coworkingapp.wallet.repository;

// Cliente externo de riesgo — dependencia a mockear
public interface RiskClient {
    boolean isBlocked(String email);
}