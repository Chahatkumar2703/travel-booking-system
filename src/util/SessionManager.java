package util;

import com.sun.net.httpserver.HttpExchange;
import model.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe session manager holding logged-in user state.
 * Supports both desktop Swing single-user sessions and multi-user web HTTP sessions with token authentication.
 */
public class SessionManager {

    // Desktop Swing single-user session
    private static User currentUser = null;

    // Web multi-user concurrent sessions
    private static final Map<String, User> webSessions = new ConcurrentHashMap<>();
    private static final Map<String, Long> sessionExpiry = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MS = 24L * 60 * 60 * 1000; // 24 hours

    // =========================================================================
    // Desktop Swing Methods
    // =========================================================================

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

    // =========================================================================
    // Web Session Token Methods
    // =========================================================================

    /**
     * Creates an active web session for an authenticated user and returns the session token.
     */
    public static String createWebSession(User user) {
        if (user == null) return null;
        String token = UUID.randomUUID().toString();
        webSessions.put(token, user);
        sessionExpiry.put(token, System.currentTimeMillis() + SESSION_TIMEOUT_MS);
        return token;
    }

    /**
     * Retrieves the authenticated user associated with a given web session token.
     */
    public static User getWebUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        token = token.trim();
        Long expiry = sessionExpiry.get(token);
        if (expiry == null || expiry < System.currentTimeMillis()) {
            webSessions.remove(token);
            sessionExpiry.remove(token);
            return null;
        }
        return webSessions.get(token);
    }

    /**
     * Terminates a web session.
     */
    public static void removeWebSession(String token) {
        if (token != null) {
            webSessions.remove(token.trim());
            sessionExpiry.remove(token.trim());
        }
    }

    /**
     * Authenticates an incoming HTTP exchange by extracting the session token from:
     * 1. Authorization: Bearer &lt;token&gt; header
     * 2. X-Session-Token header
     * 3. Cookie header (session_token=&lt;token&gt;)
     * 4. Query string (?token=&lt;token&gt;)
     */
    public static User authenticateRequest(HttpExchange exchange) {
        if (exchange == null) return null;

        // 1. Check Authorization header: Bearer <token>
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String token = authHeader.substring(7).trim();
            User user = getWebUser(token);
            if (user != null) return user;
        }

        // 2. Check X-Session-Token header
        String customHeader = exchange.getRequestHeaders().getFirst("X-Session-Token");
        if (customHeader != null && !customHeader.trim().isEmpty()) {
            User user = getWebUser(customHeader.trim());
            if (user != null) return user;
        }

        // 3. Check Cookie: session_token=<token>
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieLine : cookieHeaders) {
                String[] cookies = cookieLine.split(";");
                for (String c : cookies) {
                    String[] parts = c.trim().split("=", 2);
                    if (parts.length == 2 && "session_token".equalsIgnoreCase(parts[0].trim())) {
                        User user = getWebUser(parts[1].trim());
                        if (user != null) return user;
                    }
                }
            }
        }

        // 4. Check Query Parameter: ?token=<token>
        String query = exchange.getRequestURI().getQuery();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    if ("token".equalsIgnoreCase(key) || "sessionToken".equalsIgnoreCase(key)) {
                        User user = getWebUser(parts[1].trim());
                        if (user != null) return user;
                    }
                }
            }
        }

        return null;
    }
}
