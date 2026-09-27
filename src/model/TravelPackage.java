package model;

/**
 * TravelPackage entity representing tour packages with duration, costs, and inclusions.
 */
public class TravelPackage extends BaseEntity {

    private int agentId;
    private String agentName; // Populated via SQL JOIN
    private String packageName;
    private int destinationId;
    private String destinationName; // Populated via SQL JOIN
    private int durationDays;
    private int durationNights;
    private double pricePerPerson;
    private String placesCovered;
    private boolean hotelIncluded;
    private boolean foodIncluded;
    private boolean transportIncluded;
    private String description;
    private String imageUrl;
    private String status;         // "ACTIVE" or "INACTIVE"
    private String approvalStatus; // "PENDING", "APPROVED", "REJECTED"

    public TravelPackage() {
        super();
        this.agentId = 1;
        this.hotelIncluded = true;
        this.foodIncluded = true;
        this.transportIncluded = true;
        this.imageUrl = "";
        this.status = "ACTIVE";
        this.approvalStatus = "APPROVED";
    }

    public TravelPackage(int id, String packageName, int destinationId, String destinationName,
                         int durationDays, int durationNights, double pricePerPerson,
                         String placesCovered, boolean hotelIncluded, boolean foodIncluded,
                         boolean transportIncluded, String description, String status) {
        super(id);
        this.agentId = 1;
        this.packageName = packageName;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.durationDays = durationDays;
        this.durationNights = durationNights;
        this.pricePerPerson = pricePerPerson;
        this.placesCovered = placesCovered;
        this.hotelIncluded = hotelIncluded;
        this.foodIncluded = foodIncluded;
        this.transportIncluded = transportIncluded;
        this.description = description;
        this.imageUrl = "";
        this.status = status != null ? status : "ACTIVE";
        this.approvalStatus = "APPROVED";
    }

    public TravelPackage(int id, int agentId, String agentName, String packageName,
                         int destinationId, String destinationName, int durationDays,
                         int durationNights, double pricePerPerson, String placesCovered,
                         boolean hotelIncluded, boolean foodIncluded, boolean transportIncluded,
                         String description, String imageUrl, String status, String approvalStatus) {
        super(id);
        this.agentId = agentId;
        this.agentName = agentName;
        this.packageName = packageName;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.durationDays = durationDays;
        this.durationNights = durationNights;
        this.pricePerPerson = pricePerPerson;
        this.placesCovered = placesCovered;
        this.hotelIncluded = hotelIncluded;
        this.foodIncluded = foodIncluded;
        this.transportIncluded = transportIncluded;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getDurationNights() {
        return durationNights;
    }

    public void setDurationNights(int durationNights) {
        this.durationNights = durationNights;
    }

    public double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setPricePerPerson(double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    public String getPlacesCovered() {
        return placesCovered;
    }

    public void setPlacesCovered(String placesCovered) {
        this.placesCovered = placesCovered;
    }

    public boolean isHotelIncluded() {
        return hotelIncluded;
    }

    public void setHotelIncluded(boolean hotelIncluded) {
        this.hotelIncluded = hotelIncluded;
    }

    public boolean isFoodIncluded() {
        return foodIncluded;
    }

    public void setFoodIncluded(boolean foodIncluded) {
        this.foodIncluded = foodIncluded;
    }

    public boolean isTransportIncluded() {
        return transportIncluded;
    }

    public void setTransportIncluded(boolean transportIncluded) {
        this.transportIncluded = transportIncluded;
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

    public String getDurationSummary() {
        return durationDays + "D / " + durationNights + "N";
    }

    @Override
    public String toString() {
        return packageName + " (" + durationDays + "D/" + durationNights + "N - ₹" + pricePerPerson + ")";
    }
}
