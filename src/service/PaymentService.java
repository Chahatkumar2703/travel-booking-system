package service;

import dao.PaymentDAO;
import model.Payment;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * High-level service orchestrating payment processing and recording.
 */
public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final Map<String, PaymentProcessor> processors;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.processors = new HashMap<>();

        // Register polymorphic processors
        processors.put("UPI", new UpiPaymentProcessor());
        processors.put("CARD", new CardPaymentProcessor());
        processors.put("NET_BANKING", new NetBankingPaymentProcessor());
        processors.put("CASH", new CashPaymentProcessor());
    }

    /**
     * Executes the simulated payment and stores the transaction log in database.
     */
    public PaymentResult executePayment(int bookingId, int userId, double amount,
                                       String method, String details)
            throws ValidationException, DatabaseException {

        PaymentProcessor processor = processors.get(method.toUpperCase());
        if (processor == null) {
            throw new ValidationException("Unsupported payment method: " + method);
        }

        // Execute payment simulation logic
        PaymentResult result = processor.processPayment(amount, details);

        // Record payment in database
        Payment payment = new Payment();
        payment.setTransactionCode(result.getTransactionCode());
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod(processor.getMethodName());
        payment.setPaymentDetails(details);
        payment.setPaymentStatus(result.getStatus());

        try {
            boolean saved = paymentDAO.save(payment);
            if (!saved) {
                throw new DatabaseException("Failed to record transaction in database.", null);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error recording payment: " + e.getMessage(), e);
        }

        return result;
    }

    public Payment getPaymentByBookingId(int bookingId) throws DatabaseException {
        try {
            return paymentDAO.findByBookingId(bookingId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving payment for booking ID: " + bookingId, e);
        }
    }
}
