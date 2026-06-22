package ec.edu.espe.coworkingapp.reactive.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkspaceReading {

    // Nombre de la sala
    private String workspaceId;

    // Cantidad de reservas confirmadas de la sala ese día
    private Integer reservationCount;

    // % de la jornada operativa que está reservada ese día (horas reservadas ÷ horas jornada)
    private Double occupancyPercentage;

    // Día al que corresponde la lectura
    private LocalDate day;

    // Momento en que se generó la lectura
    private LocalDateTime timestamp;

    public WorkspaceReading() {}

    public WorkspaceReading(String workspaceId, Integer reservationCount, Double occupancyPercentage,
                            LocalDate day, LocalDateTime timestamp) {
        this.workspaceId = workspaceId;
        this.reservationCount = reservationCount;
        this.occupancyPercentage = occupancyPercentage;
        this.day = day;
        this.timestamp = timestamp;
    }

    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public Integer getReservationCount() { return reservationCount; }
    public void setReservationCount(Integer reservationCount) { this.reservationCount = reservationCount; }
    public Double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(Double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}