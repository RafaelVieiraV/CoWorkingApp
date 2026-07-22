package ec.edu.espe.coworkingapp.wallet.dto;

// DTO de confirmación de operación
public class WalletConfirmationDto {

    private String id;
    private double balance;

    public WalletConfirmationDto(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() { return id; }
    public double getBalance() { return balance; }
}