package service;

/**
 * Thrown when business validation fails.
 */
public class ValidationException extends TravelException {

    public ValidationException(String message) {
        super(message);
    }
}
