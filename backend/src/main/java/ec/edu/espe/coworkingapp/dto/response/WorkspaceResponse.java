package ec.edu.espe.coworkingapp.dto.response;

import ec.edu.espe.coworkingapp.domain.Workspace;
import java.math.BigDecimal;

public class WorkspaceResponse {
    private Long id;
    private String name;
    private Workspace.WorkspaceType type;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private Integer floor;
    private Boolean available;
    private String description;

    public static WorkspaceResponse from(Workspace w) {
        WorkspaceResponse r = new WorkspaceResponse();
        r.id = w.getId();
        r.name = w.getName();
        r.type = w.getType();
        r.capacity = w.getCapacity();
        r.pricePerHour = w.getPricePerHour();
        r.floor = w.getFloor();
        r.available = w.getAvailable();
        r.description = w.getDescription();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Workspace.WorkspaceType getType() { return type; }
    public Integer getCapacity() { return capacity; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public Integer getFloor() { return floor; }
    public Boolean getAvailable() { return available; }
    public String getDescription() { return description; }
}