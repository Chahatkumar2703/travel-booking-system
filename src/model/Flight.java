package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Flight entity representing scheduled flight listings provided by travel agents.
 */
public class Flight extends BaseEntity {

    private int agentId;
    private String agentName; // Populated via JOIN with users
    private String airline;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private String departureTime;
    private String arrivalTime;
    private double price;
    private int availableSeats;
    private String imageUrl;
    private String status;         // "ACTIVE", "INACTIVE"
    private String approvalStatus; // "PENDING", "APPROVED", "REJECTED"

    public Flight() {
        super();
        this.availableSeats = 60;
        this.status = "ACTIVE";
        this.approvalStatus = "PENDING";
    }

    public Flight(int id, int agentId, String airline, String flightNumber,
                  String origin, String destination, LocalDate departureDate,
                  String departureTime, String arrivalTime, double price,
                  int availableSeats, String imageUrl, String status,
                  String approvalStatus, LocalDateTime createdAt) {
        super(id);
        this.agentId = agentId;
        this.airline = airline;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.availableSeats = availableSeats;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.status = status != null ? status : "ACTIVE";
        this.approvalStatus = approvalStatus != null ? approvalStatus : "PENDING";
        this.createdAt = createdAt;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(approvalStatus);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
