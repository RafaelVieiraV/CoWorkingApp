package ec.edu.espe.coworkingapp.wallet.model;

import java.util.UUID;

// Modelo de cuenta de billetera
public class Wallet {

    private String id;
    private String ownerEmail;
    private double balance;

    // Constructor completo con id aleatorio
    public Wallet(String ownerEmail, double balance) {
        this.id = UUID.randomUUID().toString();
        this.ownerEmail = ownerEmail;
        this.balance = balance;
    }

    // Getters
    public String getId() { return id; }
    public String getOwnerEmail() { return ownerEmail; }
    public double getBalance() { return balance; }

    // Depositar dinero
    public void deposit(double amount) {
        this.balance += amount;
    }

    // Retirar dinero
    public void withdraw(double amount) {
        this.balance -= amount;
    }
}