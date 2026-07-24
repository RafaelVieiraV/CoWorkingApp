package ec.edu.espe.coworkingapp.wallet;

import ec.edu.espe.coworkingapp.wallet.dto.WalletConfirmationDto;
import ec.edu.espe.coworkingapp.wallet.model.Wallet;
import ec.edu.espe.coworkingapp.wallet.repository.RiskClient;
import ec.edu.espe.coworkingapp.wallet.repository.WalletRepository;
import ec.edu.espe.coworkingapp.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private RiskClient riskClient;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        // Inicializar mocks antes de cada prueba
        walletRepository = mock(WalletRepository.class);
        riskClient = mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    // ─── Crear cuenta con datos válidos ───
    @Test
    void createWallet_validData_savesAndReturnsDto() {
        // Arrange
        when(riskClient.isBlocked("rafa@espe.edu.ec")).thenReturn(false);
        when(walletRepository.existsByOwnerEmail("rafa@espe.edu.ec")).thenReturn(false);
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        WalletConfirmationDto result = walletService.createWallet("rafa@espe.edu.ec", 100.0);

        // Assert
        assertNotNull(result);
        assertEquals(100.0, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    // ─── Crear cuenta con email inválido ───
    @Test
    void createWallet_invalidEmail_throwsException() {
        // Arrange - Act - Assert
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet("emailsinArroba", 100.0));

        // Verificar que no se llamó a ninguna dependencia
        verifyNoInteractions(riskClient);
        verifyNoInteractions(walletRepository);
    }

    // ─── Depositar a cuenta inexistente ───
    @Test
    void deposit_walletNotFound_throwsException() {
        // Arrange
        when(walletRepository.findById("id-inexistente")).thenReturn(Optional.empty());

        // Act - Assert
        assertThrows(IllegalArgumentException.class,
                () -> walletService.deposit("id-inexistente", 50.0));
    }

    // ─── Depositar y verificar balance con captor ───
    @Test
    void deposit_validWallet_updatesBalanceAndSaves() {
        // Arrange
        Wallet wallet = new Wallet("rafa@espe.edu.ec", 100.0);
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        double newBalance = walletService.deposit(wallet.getId(), 50.0);

        // Assert
        assertEquals(150.0, newBalance);

        // Captor para verificar que se guardó el wallet actualizado
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertEquals(150.0, captor.getValue().getBalance());
    }

    // ─── Withdraw exitoso ───
    @Test
    void withdraw_validWallet_updatesBalance() {
        // Arrange
        Wallet wallet = new Wallet("rafa@espe.edu.ec", 200.0);
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        double newBalance = walletService.withdraw(wallet.getId(), 80.0);

        // Assert
        assertEquals(120.0, newBalance);
        verify(walletRepository).save(any(Wallet.class));
    }

    // ─── Withdraw con fondos insuficientes ───
    @Test
    void withdraw_insufficientFunds_throwsException() {
        // Arrange
        Wallet wallet = new Wallet("rafa@espe.edu.ec", 50.0);
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        // Act - Assert
        assertThrows(IllegalStateException.class,
                () -> walletService.withdraw(wallet.getId(), 100.0));
    }
}