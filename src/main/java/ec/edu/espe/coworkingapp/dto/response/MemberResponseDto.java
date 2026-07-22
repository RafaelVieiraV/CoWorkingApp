package ec.edu.espe.coworkingapp.dto.response;

import ec.edu.espe.coworkingapp.domain.PlanType;
import java.time.LocalDateTime;

public class MemberResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private PlanType planType;
    private Integer monthlyHoursQuota;
    private Boolean active;
    private double usedHoursThisMonth;
    private boolean quotaWarning;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }
    public Integer getMonthlyHoursQuota() { return monthlyHoursQuota; }
    public void setMonthlyHoursQuota(Integer monthlyHoursQuota) { this.monthlyHoursQuota = monthlyHoursQuota; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public double getUsedHoursThisMonth() { return usedHoursThisMonth; }
    public void setUsedHoursThisMonth(double usedHoursThisMonth) { this.usedHoursThisMonth = usedHoursThisMonth; }
    public boolean isQuotaWarning() { return quotaWarning; }
    public void setQuotaWarning(boolean quotaWarning) { this.quotaWarning = quotaWarning; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
