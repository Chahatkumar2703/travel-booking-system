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
        // 1. Prioritize native discrete cloud environment variables (Railway / Render / Docker)
        String host = getFirstEnv("MYSQLHOST", "DB_HOST");
        String port = getFirstEnv("MYSQLPORT", "DB_PORT");
        String dbName = getFirstEnv("MYSQLDATABASE", "DB_NAME");
        String envUser = getFirstEnv("MYSQLUSER", "DB_USER", "MYSQL_USER");
        String envPass = getFirstEnv("MYSQLPASSWORD", "DB_PASSWORD", "MYSQL_PASSWORD");
        String envUrl = getFirstEnv("DB_URL", "DATABASE_URL", "MYSQL_URL");

        if (host != null && !host.trim().isEmpty()) {
            if (port == null || port.trim().isEmpty()) port = "3306";
            if (dbName == null || dbName.trim().isEmpty()) dbName = "travel_booking_system";
            dbUrl = "jdbc:mysql://" + host.trim() + ":" + port.trim() + "/" + dbName.trim() +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=10000&socketTimeout=30000";
        } else if (envUrl != null && !envUrl.trim().isEmpty()) {
            envUrl = envUrl.trim();
            // Robust parsing of mysql://[user[:password]@]host[:port][/database][?params] without URI syntax failures
            String s = envUrl;
            if (s.startsWith("jdbc:mysql://")) {
                s = s.substring("jdbc:mysql://".length());
            } else if (s.startsWith("mysql://")) {
                s = s.substring("mysql://".length());
            }
            int atIdx = s.lastIndexOf('@');
            if (atIdx != -1) {
                String userPass = s.substring(0, atIdx);
                s = s.substring(atIdx + 1);
                int colonIdx = userPass.indexOf(':');
                if (colonIdx != -1) {
                    if (envUser == null || envUser.isEmpty()) envUser = userPass.substring(0, colonIdx);
                    if (envPass == null || envPass.isEmpty()) envPass = userPass.substring(colonIdx + 1);
                } else {
                    if (envUser == null || envUser.isEmpty()) envUser = userPass;
                }
            }
            String hostPort = s;
            String dbPath = "";
            int slashIdx = s.indexOf('/');
            if (slashIdx != -1) {
                hostPort = s.substring(0, slashIdx);
                dbPath = s.substring(slashIdx + 1);
                int qIdx = dbPath.indexOf('?');
                if (qIdx != -1) {
                    dbPath = dbPath.substring(0, qIdx);
                }
            }
            if (dbPath.isEmpty()) dbPath = "railway";
            dbUrl = "jdbc:mysql://" + hostPort + "/" + dbPath +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=10000&socketTimeout=30000";
        }

        if (envUser != null && !envUser.trim().isEmpty()) {
            dbUser = envUser.trim();
        }
        if (envPass != null) {
            dbPassword = envPass;
        }

        // 2. If no environment variables were provided, fall back to db.properties
        if (host == null && envUrl == null) {
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

    /**
     * Direct connection used internally during initialization without re-triggering init checks
     */
    public static Connection getConnectionDirect() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        if (!DatabaseInitializer.isInitialized()) {
            DatabaseInitializer.ensureInitialized();
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
            int lastSlash = dbUrl.lastIndexOf('/');
            int queryStart = dbUrl.indexOf('?');
            if (lastSlash > "jdbc:mysql://".length() && queryStart > lastSlash) {
                serverUrl = dbUrl.substring(0, lastSlash + 1) + dbUrl.substring(queryStart);
            } else if (lastSlash > "jdbc:mysql://".length() && queryStart == -1) {
                serverUrl = dbUrl.substring(0, lastSlash + 1) + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            } else {
                serverUrl = dbUrl;
            }
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
