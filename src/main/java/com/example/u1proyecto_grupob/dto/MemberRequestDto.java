package com.example.u1proyecto_grupob.dto;

import com.example.u1proyecto_grupob.domain.PlanType;
import jakarta.validation.constraints.*;

public class MemberRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "...")
    private String fullName;

    @NotBlank
    @Email(message = "Formato de email inválido")
    @Size(max = 150)
    private String email;

    private String phone;

    @NotNull(message = "El tipo de plan es obligatorio")
    private PlanType planType;

    @NotNull
    @Min(value = 1, message = "El cupo mínimo es 1 hora")
    private Integer monthlyHoursQuota;

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
}
