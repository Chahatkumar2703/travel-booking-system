package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity using JDBC.
 * Implements Singleton connection factory with configurable properties.
 */
public class DatabaseConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/travel_booking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    private static boolean driverLoaded = false;

    static {
        loadConfig();
        loadDriver();
    }

    private static void loadConfig() {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                if (prop.getProperty("db.url") != null) dbUrl = prop.getProperty("db.url");
                if (prop.getProperty("db.user") != null) dbUser = prop.getProperty("db.user");
                if (prop.getProperty("db.password") != null) dbPassword = prop.getProperty("db.password");
            }
        } catch (Exception ignored) {
            // Fallback to default credentials
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Connect to MySQL server without specifying database (used for initialization)
     */
    public static Connection getServerConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(serverUrl, dbUser, dbPassword);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void setCredentials(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
    }
}
