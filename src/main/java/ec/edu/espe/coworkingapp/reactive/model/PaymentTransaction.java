package ec.edu.espe.coworkingapp.reactive.model;

import java.time.LocalDateTime;

public class PaymentTransaction {

    private Long id;
    private Long bookingId;        // reserva asociada (null si es un pago manual de prueba)
    private String workspaceName;
    private String memberName;
    private Double amount;         // monto del pago
    private String status;         // ACTIVA | CANCELADA
    private LocalDateTime timestamp;

    public PaymentTransaction() {}

    public PaymentTransaction(Long id, Long bookingId, String workspaceName, String memberName,
                              Double amount, String status, LocalDateTime timestamp) {
        this.id = id;
        this.bookingId = bookingId;
        this.workspaceName = workspaceName;
        this.memberName = memberName;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
