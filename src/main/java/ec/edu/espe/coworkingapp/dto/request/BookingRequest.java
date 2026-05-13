package ec.edu.espe.coworkingapp.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class BookingRequest {

    @NotNull(message = "El ID del miembro es obligatorio")
    private Long memberId;

    @NotNull(message = "El ID del workspace es obligatorio")
    private Long workspaceId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDatetime;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime endDatetime;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public LocalDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(LocalDateTime v) { this.startDatetime = v; }
    public LocalDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(LocalDateTime v) { this.endDatetime = v; }
}