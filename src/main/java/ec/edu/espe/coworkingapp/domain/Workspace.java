package ec.edu.espe.coworkingapp.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "workspaces")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    private WorkspaceType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "price_per_hour", precision = 8, scale = 2)
    private BigDecimal pricePerHour;

    @Column
    private Integer floor;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(length = 255)
    private String description;

    public enum WorkspaceType { DESK, OFFICE, MEETING_ROOM, EVENT_HALL }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public WorkspaceType getType() { return type; }
    public void setType(WorkspaceType type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal v) { this.pricePerHour = v; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}