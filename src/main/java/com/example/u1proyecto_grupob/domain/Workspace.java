package com.example.u1proyecto_grupob.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "workspaces")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "price_per_hour", nullable = false)
    private Double pricePerHour;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(length = 255)
    private String description;

    public Workspace() {}

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
