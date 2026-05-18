package ec.edu.espe.coworkingapp.dto.request;

import ec.edu.espe.coworkingapp.domain.PlanType;
import jakarta.validation.constraints.*;

public class MemberRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ]+\\s+[A-Za-záéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre debe contener al menos un nombre y un apellido")
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email invlido")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^09\\d{8}$", message = "El teléfono (celular) debe empezar con 09 y tener exactamente 10 dígitos")
    private String phone;

    @NotNull(message = "El tipo de plan es obligatorio")
    private PlanType planType;

    @NotNull(message = "El cupo mensual es obligatorio")
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
