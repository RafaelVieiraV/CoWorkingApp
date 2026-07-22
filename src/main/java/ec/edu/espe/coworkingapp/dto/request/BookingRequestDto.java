package ec.edu.espe.coworkingapp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BookingRequestDto {

    @NotNull(message = "El miembro es obligatorio")
    private Long memberId;

    @NotNull(message = "El workspace es obligatorio")
    private Long workspaceId;

    @NotNull(message = "La fecha/hora de inicio es obligatoria")
    private LocalDateTime startDatetime;

    @NotNull(message = "La fecha/hora de fin es obligatoria")
    private LocalDateTime endDatetime;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public LocalDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(LocalDateTime startDatetime) { this.startDatetime = startDatetime; }
    public LocalDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(LocalDateTime endDatetime) { this.endDatetime = endDatetime; }
}
