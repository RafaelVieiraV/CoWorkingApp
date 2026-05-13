package ec.edu.espe.coworkingapp.dto.request;

import ec.edu.espe.coworkingapp.domain.Workspace;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class WorkspaceRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @NotNull(message = "El tipo es obligatorio")
    private Workspace.WorkspaceType type;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(1)
    private Integer capacity;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin("0.0")
    private BigDecimal pricePerHour;

    private Integer floor;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Workspace.WorkspaceType getType() { return type; }
    public void setType(Workspace.WorkspaceType type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal v) { this.pricePerHour = v; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}