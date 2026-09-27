package model;

import java.time.LocalDateTime;

/**
 * Rental Car entity representing vehicles available for rent by travel agents.
 */
public class Car extends BaseEntity {

    private int agentId;
    private String agentName; // Populated via JOIN with users
    private String carName;
    private String brand;
    private String model;
    private String location;
    private String carType; // "SUV", "Sedan", "Hatchback", "Luxury"
    private double pricePerDay;
    private int availableUnits;
    private String imageUrl;
    private String status;         // "ACTIVE", "INACTIVE"
    private String approvalStatus; // "PENDING", "APPROVED", "REJECTED"

    public Car() {
        super();
        this.carType = "Sedan";
        this.availableUnits = 5;
        this.status = "ACTIVE";
        this.approvalStatus = "PENDING";
    }

    public Car(int id, int agentId, String carName, String brand,
               String model, String location, String carType,
               double pricePerDay, int availableUnits, String imageUrl,
               String status, String approvalStatus, LocalDateTime createdAt) {
        super(id);
        this.agentId = agentId;
        this.carName = carName;
        this.brand = brand;
        this.model = model;
        this.location = location;
        this.carType = carType;
        this.pricePerDay = pricePerDay;
        this.availableUnits = availableUnits;
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

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(int availableUnits) {
        this.availableUnits = availableUnits;
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
