package service;

import java.util.UUID;

/**
 * Handles simulated Credit & Debit card payments with validation.
 */
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(double amount, String cardInfo) throws ValidationException {
        // cardInfo format: "cardNumber|expiry|cvv|cardHolder"
        if (cardInfo == null || !cardInfo.contains("|")) {
            throw new ValidationException("Invalid card details provided.");
        }

        String[] parts = cardInfo.split("\\|");
        String cardNumber = parts.length > 0 ? parts[0].replaceAll("[\\s-]", "") : "";
        String expiry = parts.length > 1 ? parts[1].trim() : "";
        String cvv = parts.length > 2 ? parts[2].trim() : "";
        String cardHolder = parts.length > 3 ? parts[3].trim() : "";

        if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
            throw new ValidationException("Card number must be exactly 16 numeric digits.");
        }
        if (!expiry.matches("^(0[1-9]|1[0-2])\\/?([0-9]{2})$")) {
            throw new ValidationException("Expiry date must be in MM/YY format.");
        }
        if (cvv.length() != 3 || !cvv.matches("\\d{3}")) {
            throw new ValidationException("CVV must be 3 numeric digits.");
        }
        if (cardHolder.isEmpty()) {
            throw new ValidationException("Cardholder name is required.");
        }

        String last4 = cardNumber.substring(12);
        String txnCode = "TXN-CRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, "SUCCESS", txnCode, "Card payment charged successfully for Card ending with " + last4);
    }

    @Override
    public String getMethodName() {
        return "CARD";
    }
}
