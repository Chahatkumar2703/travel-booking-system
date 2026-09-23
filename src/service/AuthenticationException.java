package service;

/**
 * Thrown when user credentials or authorization fails.
 */
public class AuthenticationException extends TravelException {

    public AuthenticationException(String message) {
        super(message);
    }
}
