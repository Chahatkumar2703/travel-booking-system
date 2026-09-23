package service;

/**
 * Strategy pattern interface for payment processing, demonstrating Polymorphism.
 */
public interface PaymentProcessor {

    /**
     * Executes the payment simulation.
     *
     * @param amount  total amount to pay
     * @param details method-specific payment details (e.g. UPI ID, card number, bank name)
     * @return PaymentResult containing status, transaction code, and response message
     * @throws ValidationException if input details are invalid
     */
    PaymentResult processPayment(double amount, String details) throws ValidationException;

    /**
     * Returns the standardized payment method identifier.
     */
    String getMethodName();
}
