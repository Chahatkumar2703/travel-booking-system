package util;

import model.User;

/**
 * Thread-safe session manager holding logged-in user state.
 */
public class SessionManager {

    private static User currentUser = null;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static synchronized void logout() {
        currentUser = null;
    }
}
