package model;

/**
 * TravelPackage entity representing tour packages with duration, costs, and inclusions.
 */
public class TravelPackage extends BaseEntity {

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
    private String status; // "ACTIVE" or "INACTIVE"

    public TravelPackage() {
        super();
        this.hotelIncluded = true;
        this.foodIncluded = true;
        this.transportIncluded = true;
        this.status = "ACTIVE";
    }

    public TravelPackage(int id, String packageName, int destinationId, String destinationName,
                         int durationDays, int durationNights, double pricePerPerson,
                         String placesCovered, boolean hotelIncluded, boolean foodIncluded,
                         boolean transportIncluded, String description, String status) {
        super(id);
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
        this.status = status != null ? status : "ACTIVE";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDurationSummary() {
        return durationDays + " Days / " + durationNights + " Nights";
    }

    @Override
    public String toString() {
        return packageName + " (" + getDurationSummary() + ")";
    }
}
