package com.example.u1proyecto_grupob.dto;

import com.example.u1proyecto_grupob.domain.BookingStatus;
import com.example.u1proyecto_grupob.domain.WorkspaceType;
import java.time.LocalDateTime;

public class BookingResponseDto {

    private Long id;
    private Long memberId;
    private String memberFullName;
    private Long workspaceId;
    private String workspaceName;
    private WorkspaceType workspaceType;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Double totalHours;
    private Double totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberFullName() { return memberFullName; }
    public void setMemberFullName(String memberFullName) { this.memberFullName = memberFullName; }

    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }

    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }

    public WorkspaceType getWorkspaceType() { return workspaceType; }
    public void setWorkspaceType(WorkspaceType workspaceType) { this.workspaceType = workspaceType; }

    public LocalDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(LocalDateTime startDatetime) { this.startDatetime = startDatetime; }

    public LocalDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(LocalDateTime endDatetime) { this.endDatetime = endDatetime; }

    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
