package ec.edu.espe.coworkingapp.reactive.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkspaceReading {

    // Nombre de la sala
    private String workspaceId;

    // Porcentaje de ocupación de la sala ESE día (0.0 a 100.0)
    private Double occupancyPercentage;

    // Día al que corresponde la ocupación
    private LocalDate day;

    // Momento en que se generó la lectura
    private LocalDateTime timestamp;

    public WorkspaceReading() {}

    public WorkspaceReading(String workspaceId, Double occupancyPercentage, LocalDate day, LocalDateTime timestamp) {
        this.workspaceId = workspaceId;
        this.occupancyPercentage = occupancyPercentage;
        this.day = day;
        this.timestamp = timestamp;
    }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public Double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(Double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}