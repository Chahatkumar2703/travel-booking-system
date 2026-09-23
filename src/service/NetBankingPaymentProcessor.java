package service;

import java.util.UUID;

/**
 * Handles simulated Net Banking transactions.
 */
public class NetBankingPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(double amount, String bankName) throws ValidationException {
        if (bankName == null || bankName.trim().isEmpty() || "Select Bank".equalsIgnoreCase(bankName.trim())) {
            throw new ValidationException("Please select your bank for Net Banking transfer.");
        }

        String txnCode = "TXN-NB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, "SUCCESS", txnCode, "Net Banking authentication successful via " + bankName.trim());
    }

    @Override
    public String getMethodName() {
        return "NET_BANKING";
    }
}
