package ec.edu.espe.coworkingapp.dto.response;

import ec.edu.espe.coworkingapp.domain.Booking;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long workspaceId;
    private String workspaceName;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Booking.BookingStatus status;
    private BigDecimal totalHours;
    private LocalDateTime createdAt;

    public static BookingResponse from(Booking b) {
        BookingResponse r = new BookingResponse();
        r.id = b.getId();
        r.memberId = b.getMember().getId();
        r.memberName = b.getMember().getFullName();
        r.workspaceId = b.getWorkspace().getId();
        r.workspaceName = b.getWorkspace().getName();
        r.startDatetime = b.getStartDatetime();
        r.endDatetime = b.getEndDatetime();
        r.status = b.getStatus();
        r.totalHours = b.getTotalHours();
        r.createdAt = b.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Long getWorkspaceId() { return workspaceId; }
    public String getWorkspaceName() { return workspaceName; }
    public LocalDateTime getStartDatetime() { return startDatetime; }
    public LocalDateTime getEndDatetime() { return endDatetime; }
    public Booking.BookingStatus getStatus() { return status; }
    public BigDecimal getTotalHours() { return totalHours; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}