package ec.edu.espe.coworkingapp.reactive.model;

import java.time.LocalDateTime;

public class WorkspaceReading {

    // Identificador del workspace monitoreado
    private String workspaceId;

    // Porcentaje de ocupación (0.0 a 100.0)
    private Double occupancyPercentage;

    // Momento en que se registró la lectura
    private LocalDateTime timestamp;

    public WorkspaceReading() {}

    public WorkspaceReading(String workspaceId, Double occupancyPercentage, LocalDateTime timestamp) {
        this.workspaceId = workspaceId;
        this.occupancyPercentage = occupancyPercentage;
        this.timestamp = timestamp;
    }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public Double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(Double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}