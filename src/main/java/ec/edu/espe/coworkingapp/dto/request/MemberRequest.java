package ec.edu.espe.coworkingapp.dto.request;

import ec.edu.espe.coworkingapp.domain.Member;
import jakarta.validation.constraints.*;

public class MemberRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @Size(max = 20)
    private String phone;

    private Member.PlanType planType;
    private Integer monthlyHoursQuota;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Member.PlanType getPlanType() { return planType; }
    public void setPlanType(Member.PlanType planType) { this.planType = planType; }
    public Integer getMonthlyHoursQuota() { return monthlyHoursQuota; }
    public void setMonthlyHoursQuota(Integer v) { this.monthlyHoursQuota = v; }
}