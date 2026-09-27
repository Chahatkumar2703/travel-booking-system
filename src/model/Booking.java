package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Universal Booking entity representing reservations for Packages, Flights, Hotels, and Cars.
 */
public class Booking extends BaseEntity {

    private String bookingCode;
    private int userId;
    private String userName;     // JOIN with users
    private String userEmail;    // JOIN with users
    private String bookingType;  // "PACKAGE", "FLIGHT", "HOTEL", "CAR"
    private Integer itemId;      // Generic item ID
    private String itemName;     // Generic item description/title
    private Integer flightId;    // Nullable
    private Integer hotelId;     // Nullable
    private Integer carId;       // Nullable
    private Integer packageId;   // Nullable
    private Integer agentId;     // Nullable
    private String agentName;    // JOIN with users
    private String packageName;  // JOIN with packages
    private String destinationName; // JOIN with destinations
    private String hotelName;    // JOIN with hotels
    private LocalDate travelDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private int persons;
    private int quantity;
    private double packageCost;
    private double hotelCost;
    private double totalAmount;
    private String specialRequests;
    private String bookingStatus; // "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
    private String paymentStatus; // "SUCCESS", "PENDING", "FAILED"
    private LocalDateTime updatedAt;

    public Booking() {
        super();
        this.bookingType = "PACKAGE";
        this.persons = 1;
        this.quantity = 1;
        this.bookingStatus = "CONFIRMED";
        this.paymentStatus = "SUCCESS";
    }

    public Booking(int id, String bookingCode, int userId, String userName, String userEmail,
                   int packageId, String packageName, String destinationName,
                   Integer hotelId, String hotelName, LocalDate travelDate, int persons,
                   double packageCost, double hotelCost, double totalAmount,
                   String specialRequests, String bookingStatus, LocalDateTime createdAt) {
        super(id);
        this.bookingCode = bookingCode;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.bookingType = "PACKAGE";
        this.packageId = packageId;
        this.packageName = packageName;
        this.itemName = packageName;
        this.destinationName = destinationName;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.travelDate = travelDate;
        this.startDate = travelDate;
        this.persons = persons;
        this.quantity = persons;
        this.packageCost = packageCost;
        this.hotelCost = hotelCost;
        this.totalAmount = totalAmount;
        this.specialRequests = specialRequests;
        this.bookingStatus = bookingStatus != null ? bookingStatus : "CONFIRMED";
        this.paymentStatus = "SUCCESS";
        this.createdAt = createdAt;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getBookingType() {
        return bookingType != null ? bookingType : "PACKAGE";
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        if (itemName != null && !itemName.isEmpty()) return itemName;
        if (packageName != null && !packageName.isEmpty()) return packageName;
        if (hotelName != null && !hotelName.isEmpty()) return hotelName;
        return "Booking #" + getId();
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    public Integer getCarId() {
        return carId;
    }

    public void setCarId(Integer carId) {
        this.carId = carId;
    }

    public Integer getPackageId() {
        return packageId != null ? packageId : 0;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
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

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public LocalDate getStartDate() {
        return startDate != null ? startDate : travelDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getPersons() {
        return persons;
    }

    public void setPersons(int persons) {
        this.persons = persons;
    }

    public int getQuantity() {
        return quantity > 0 ? quantity : persons;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPackageCost() {
        return packageCost;
    }

    public void setPackageCost(double packageCost) {
        this.packageCost = packageCost;
    }

    public double getHotelCost() {
        return hotelCost;
    }

    public void setHotelCost(double hotelCost) {
        this.hotelCost = hotelCost;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
