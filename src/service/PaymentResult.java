package service;

/**
 * Result of a simulated payment transaction.
 */
public class PaymentResult {

    private final boolean success;
    private final String status; // "SUCCESS", "FAILED", "PENDING"
    private final String transactionCode;
    private final String message;

    public PaymentResult(boolean success, String status, String transactionCode, String message) {
        this.success = success;
        this.status = status;
        this.transactionCode = transactionCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatus() {
        return status;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getMessage() {
        return message;
    }
}
