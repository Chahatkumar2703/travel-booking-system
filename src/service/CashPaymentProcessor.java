package service;

import java.util.UUID;

/**
 * Handles Cash on Arrival / Pay Later bookings.
 */
public class CashPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(double amount, String details) {
        String txnCode = "TXN-CSH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, "PENDING", txnCode, "Booking marked as Pay On Arrival. Payment status is PENDING.");
    }

    @Override
    public String getMethodName() {
        return "CASH";
    }
}
