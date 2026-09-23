package service;

/**
 * Root checked exception for the Travel Booking Application.
 */
public class TravelException extends Exception {

    public TravelException(String message) {
        super(message);
    }

    public TravelException(String message, Throwable cause) {
        super(message, cause);
    }
}
