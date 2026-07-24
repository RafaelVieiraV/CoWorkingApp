package ec.edu.espe.coworkingapp.wallet.service;

import ec.edu.espe.coworkingapp.wallet.dto.WalletConfirmationDto;
import ec.edu.espe.coworkingapp.wallet.model.Wallet;
import ec.edu.espe.coworkingapp.wallet.repository.RiskClient;
import ec.edu.espe.coworkingapp.wallet.repository.WalletRepository;

// Servicio principal de billetera
public class WalletService {

    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    // Crear cuenta
    public WalletConfirmationDto createWallet(String email, double initialBalance) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email no válido");
        }
        if (initialBalance <= 0) {
            throw new IllegalArgumentException("Saldo inicial debe ser mayor a 0");
        }
        if (riskClient.isBlocked(email)) {
            throw new IllegalStateException("Cliente bloqueado por riesgo");
        }
        if (walletRepository.existsByOwnerEmail(email)) {
            throw new IllegalStateException("Ya existe una cuenta con ese email");
        }
        Wallet wallet = new Wallet(email, initialBalance);
        walletRepository.save(wallet);
        return new WalletConfirmationDto(wallet.getId(), wallet.getBalance());
    }

    // Depositar
    public double deposit(String walletId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        wallet.deposit(amount);
        walletRepository.save(wallet);
        return wallet.getBalance();
    }

    // Retirar (feature/withdraw)
    public double withdraw(String walletId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Fondos insuficientes");
        }
        wallet.withdraw(amount);
        walletRepository.save(wallet);
        return wallet.getBalance();
    }
}