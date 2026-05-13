package ec.edu.espe.coworkingapp.dto.response;

import ec.edu.espe.coworkingapp.domain.WorkspaceType;

public class WorkspaceResponseDto {

    private Long id;
    private String name;
    private WorkspaceType type;
    private Integer capacity;
    private Double pricePerHour;
    private Integer floor;
    private Boolean available;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
