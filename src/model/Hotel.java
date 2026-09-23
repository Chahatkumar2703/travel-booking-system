package model;

/**
 * Hotel entity representing accommodations with room types, pricing, and availability.
 */
public class Hotel extends BaseEntity {

    private String hotelName;
    private int destinationId;
    private String destinationName; // Populated via SQL JOIN
    private String address;
    private String roomType;
    private double pricePerNight;
    private int availableRooms;
    private double rating;
    private String description;
    private String status; // "ACTIVE" or "INACTIVE"

    public Hotel() {
        super();
        this.roomType = "Deluxe";
        this.availableRooms = 10;
        this.rating = 4.5;
        this.status = "ACTIVE";
    }

    public Hotel(int id, String hotelName, int destinationId, String destinationName,
                 String address, String roomType, double pricePerNight,
                 int availableRooms, double rating, String description, String status) {
        super(id);
        this.hotelName = hotelName;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.address = address;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.availableRooms = availableRooms;
        this.rating = rating;
        this.description = description;
        this.status = status != null ? status : "ACTIVE";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return hotelName + " (" + roomType + " - ₹" + pricePerNight + "/night)";
    }
}
