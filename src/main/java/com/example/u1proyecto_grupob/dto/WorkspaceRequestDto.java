package com.example.u1proyecto_grupob.dto;

import com.example.u1proyecto_grupob.domain.WorkspaceType;
import jakarta.validation.constraints.*;

public class WorkspaceRequestDto {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private WorkspaceType type;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double pricePerHour;

    @NotNull
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
