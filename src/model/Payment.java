package model;

import java.time.LocalDateTime;

/**
 * Payment entity representing transaction records.
 */
public class Payment extends BaseEntity {

    private String transactionCode;
    private int bookingId;
    private String bookingCode;  // JOIN with bookings
    private int userId;
    private String userName;     // JOIN with users
    private double amount;
    private String paymentMethod; // "UPI", "CARD", "NET_BANKING", "CASH"
    private String paymentDetails;
    private String paymentStatus; // "SUCCESS", "FAILED", "PENDING"

    public Payment() {
        super();
        this.paymentStatus = "SUCCESS";
    }

    public Payment(int id, String transactionCode, int bookingId, String bookingCode,
                   int userId, String userName, double amount, String paymentMethod,
                   String paymentDetails, String paymentStatus, LocalDateTime createdAt) {
        super(id);
        this.transactionCode = transactionCode;
        this.bookingId = bookingId;
        this.bookingCode = bookingCode;
        this.userId = userId;
        this.userName = userName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDetails = paymentDetails;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "SUCCESS";
        this.createdAt = createdAt;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
