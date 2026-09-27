package model;

/**
 * Hotel entity representing accommodations with room types, pricing, and availability.
 */
public class Hotel extends BaseEntity {

    private int agentId;
    private String agentName; // Populated via SQL JOIN
    private String hotelName;
    private int destinationId;
    private String destinationName; // Populated via SQL JOIN
    private String address;
    private String location;
    private String roomType;
    private double pricePerNight;
    private int availableRooms;
    private double rating;
    private String description;
    private String imageUrl;
    private String status;         // "ACTIVE" or "INACTIVE"
    private String approvalStatus; // "PENDING", "APPROVED", "REJECTED"

    public Hotel() {
        super();
        this.agentId = 1;
        this.roomType = "Deluxe";
        this.availableRooms = 10;
        this.rating = 4.5;
        this.status = "ACTIVE";
        this.approvalStatus = "APPROVED";
        this.imageUrl = "";
    }

    public Hotel(int id, String hotelName, int destinationId, String destinationName,
                 String address, String roomType, double pricePerNight,
                 int availableRooms, double rating, String description, String status) {
        super(id);
        this.agentId = 1;
        this.hotelName = hotelName;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.address = address;
        this.location = address;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.availableRooms = availableRooms;
        this.rating = rating;
        this.description = description;
        this.status = status != null ? status : "ACTIVE";
        this.approvalStatus = "APPROVED";
        this.imageUrl = "";
    }

    public Hotel(int id, int agentId, String agentName, String hotelName, int destinationId,
                 String destinationName, String address, String location, String roomType,
                 double pricePerNight, int availableRooms, double rating, String description,
                 String imageUrl, String status, String approvalStatus) {
        super(id);
        this.agentId = agentId;
        this.agentName = agentName;
        this.hotelName = hotelName;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.address = address;
        this.location = location != null ? location : address;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.availableRooms = availableRooms;
        this.rating = rating;
        this.description = description;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.status = status != null ? status : "ACTIVE";
        this.approvalStatus = approvalStatus != null ? approvalStatus : "APPROVED";
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

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location != null && !location.isEmpty() ? location : address;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return hotelName + " (" + roomType + " - ₹" + pricePerNight + "/night)";
    }
}
