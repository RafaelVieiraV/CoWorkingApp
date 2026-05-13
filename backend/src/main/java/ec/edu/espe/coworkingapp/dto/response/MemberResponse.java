package ec.edu.espe.coworkingapp.dto.response;

import ec.edu.espe.coworkingapp.domain.Member;
import java.time.LocalDateTime;

public class MemberResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Member.PlanType planType;
    private Integer monthlyHoursQuota;
    private Boolean active;
    private LocalDateTime createdAt;

    public static MemberResponse from(Member m) {
        MemberResponse r = new MemberResponse();
        r.id = m.getId();
        r.fullName = m.getFullName();
        r.email = m.getEmail();
        r.phone = m.getPhone();
        r.planType = m.getPlanType();
        r.monthlyHoursQuota = m.getMonthlyHoursQuota();
        r.active = m.getActive();
        r.createdAt = m.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Member.PlanType getPlanType() { return planType; }
    public Integer getMonthlyHoursQuota() { return monthlyHoursQuota; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}