package service;

/**
 * Thrown when database operations fail.
 */
public class DatabaseException extends TravelException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
