package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Booking entity representing a customer's package reservation.
 */
public class Booking extends BaseEntity {

    private String bookingCode;
    private int userId;
    private String userName;     // JOIN with users
    private String userEmail;    // JOIN with users
    private int packageId;
    private String packageName;  // JOIN with packages
    private String destinationName; // JOIN with destinations
    private Integer hotelId;     // Nullable
    private String hotelName;    // JOIN with hotels
    private LocalDate travelDate;
    private int persons;
    private double packageCost;
    private double hotelCost;
    private double totalAmount;
    private String specialRequests;
    private String bookingStatus; // "CONFIRMED", "CANCELLED", "COMPLETED"
    private String paymentStatus; // Populated from payments table if available
    private LocalDateTime updatedAt;

    public Booking() {
        super();
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
        this.packageId = packageId;
        this.packageName = packageName;
        this.destinationName = destinationName;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.travelDate = travelDate;
        this.persons = persons;
        this.packageCost = packageCost;
        this.hotelCost = hotelCost;
        this.totalAmount = totalAmount;
        this.specialRequests = specialRequests;
        this.bookingStatus = bookingStatus != null ? bookingStatus : "CONFIRMED";
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

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
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

    public int getPersons() {
        return persons;
    }

    public void setPersons(int persons) {
        this.persons = persons;
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
