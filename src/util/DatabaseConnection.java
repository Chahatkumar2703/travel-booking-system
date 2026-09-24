package util;

import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity using JDBC.
 * Supports both local development (db.properties / defaults) and cloud deployments
 * via environment variables (Docker, Render, Railway).
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
        // 1. Check cloud environment variables first (Render / Railway / Docker)
        String envUrl = getFirstEnv("DB_URL", "DATABASE_URL", "MYSQL_URL");
        String envUser = getFirstEnv("DB_USER", "MYSQLUSER", "MYSQL_USER");
        String envPass = getFirstEnv("DB_PASSWORD", "MYSQLPASSWORD", "MYSQL_PASSWORD");

        if (envUrl != null && !envUrl.trim().isEmpty()) {
            envUrl = envUrl.trim();
            if (envUrl.startsWith("mysql://")) {
                try {
                    URI uri = new URI(envUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null) {
                        String[] parts = userInfo.split(":", 2);
                        if (envUser == null || envUser.isEmpty()) envUser = parts[0];
                        if (parts.length > 1 && (envPass == null || envPass.isEmpty())) envPass = parts[1];
                    }
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 3306;
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) path = path.substring(1);
                    dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + path + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                } catch (Exception e) {
                    dbUrl = "jdbc:" + envUrl;
                }
            } else if (!envUrl.startsWith("jdbc:")) {
                dbUrl = "jdbc:mysql://" + envUrl;
            } else {
                dbUrl = envUrl;
            }
        } else {
            // Check individual host, port, db environment variables
            String host = getFirstEnv("DB_HOST", "MYSQLHOST");
            String port = getFirstEnv("DB_PORT", "MYSQLPORT");
            String dbName = getFirstEnv("DB_NAME", "MYSQLDATABASE");
            if (host != null && !host.trim().isEmpty()) {
                if (port == null || port.trim().isEmpty()) port = "3306";
                if (dbName == null || dbName.trim().isEmpty()) dbName = "travel_booking_system";
                dbUrl = "jdbc:mysql://" + host.trim() + ":" + port.trim() + "/" + dbName.trim() + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }
        }

        if (envUser != null && !envUser.trim().isEmpty()) {
            dbUser = envUser.trim();
        }
        if (envPass != null) {
            dbPassword = envPass;
        }

        // 2. If no environment variables were provided, fall back to db.properties
        if (envUrl == null && getFirstEnv("DB_HOST", "MYSQLHOST") == null) {
            try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input != null) {
                    Properties prop = new Properties();
                    prop.load(input);
                    if (prop.getProperty("db.url") != null) dbUrl = prop.getProperty("db.url");
                    if (prop.getProperty("db.user") != null) dbUser = prop.getProperty("db.user");
                    if (prop.getProperty("db.password") != null) dbPassword = prop.getProperty("db.password");
                }
            } catch (Exception ignored) {
                // Keep default credentials
            }
        }
    }

    private static String getFirstEnv(String... names) {
        for (String name : names) {
            String val = System.getenv(name);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
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
        String serverUrl;
        if (dbUrl.contains("//localhost") || dbUrl.contains("//127.0.0.1")) {
            serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        } else {
            serverUrl = dbUrl;
        }
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

    public static String getDbUrl() {
        return dbUrl;
    }
}
