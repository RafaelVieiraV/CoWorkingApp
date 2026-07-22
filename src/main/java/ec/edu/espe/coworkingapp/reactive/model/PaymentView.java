package ec.edu.espe.coworkingapp.reactive.model;

import java.time.LocalDateTime;

public class PaymentView {

    private Long bookingId;
    private String workspaceName;
    private String memberName;
    private Double amount;
    private String paymentStatus;   // PENDIENTE | PAGADO | CANCELADA
    private LocalDateTime timestamp;

    public PaymentView() {}

    public PaymentView(Long bookingId, String workspaceName, String memberName,
                       Double amount, String paymentStatus, LocalDateTime timestamp) {
        this.bookingId = bookingId;
        this.workspaceName = workspaceName;
        this.memberName = memberName;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.timestamp = timestamp;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}