package service;

import java.util.UUID;

/**
 * Handles simulated UPI payments (Google Pay, PhonePe, Paytm, BHIM).
 */
public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(double amount, String upiId) throws ValidationException {
        if (upiId == null || !upiId.contains("@") || upiId.trim().length() < 5) {
            throw new ValidationException("Please enter a valid UPI ID (e.g. yourname@okhdfcbank, user@paytm).");
        }

        String txnCode = "TXN-UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, "SUCCESS", txnCode, "UPI payment verified successfully via VPA: " + upiId.trim());
    }

    @Override
    public String getMethodName() {
        return "UPI";
    }
}
