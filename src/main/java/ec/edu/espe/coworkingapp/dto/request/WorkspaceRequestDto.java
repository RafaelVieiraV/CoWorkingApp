package ec.edu.espe.coworkingapp.dto.request;

import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import jakarta.validation.constraints.*;

public class WorkspaceRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @NotNull(message = "El tipo es obligatorio")
    private WorkspaceType type;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1")
    private Integer capacity;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private Double pricePerHour;

    @NotNull(message = "El piso es obligatorio")
    @Min(value = 1, message = "El piso mínimo es 1")
    @Max(value = 10, message = "El piso máximo es 10")
    private Integer floor;

    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public WorkspaceType getType() { return type; }
    public void setType(WorkspaceType type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
