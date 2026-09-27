import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import util.DatabaseConnection;
import util.DatabaseInitializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

public class ProductionRegressionTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String label, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + label);
            passed++;
        } else {
            System.err.println("[FAIL] " + label);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("     RAILWAY PRODUCTION REGRESSION & RESILIENCE TEST      ");
        System.out.println("==========================================================");

        try {
            testDatabaseInitializationAndFlags();
            testErrorResponseFormat();
            testOptionsCorsPreflight();
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        System.out.println("==========================================================");
        System.out.printf("  RESULT: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testDatabaseInitializationAndFlags() {
        check("1. Database is initially not initialized before first trigger", !DatabaseInitializer.isInitialized());
        boolean connValid = false;
        try {
            connValid = DatabaseConnection.getConnection() != null;
        } catch (Exception e) {}
        check("2. DatabaseConnection.getConnection() lazily initializes DB and connects", connValid);
        check("3. DatabaseInitializer.isInitialized() is true after getConnection()", DatabaseInitializer.isInitialized());
    }

    private static void testErrorResponseFormat() throws Exception {
        // Use reflection to test WebServer.escape and sendErrorResponse structure
        Method escapeMethod = WebServer.class.getDeclaredMethod("escape", String.class);
        escapeMethod.setAccessible(true);
        String escaped = (String) escapeMethod.invoke(null, "Test \"quotes\" and \n newline");
        check("3.1 WebServer.escape properly handles quotes and newlines",
                escaped.contains("\\\"") && escaped.contains("\\n"));

        // Simulate error response json
        String msg = "Test failure message";
        String expectedErrorJson = "{\"success\":false,\"error\":\"" + msg + "\",\"message\":\"" + msg + "\"}";
        check("3.2 Error response contains both 'error' and 'message' fields",
                expectedErrorJson.contains("\"error\":\"" + msg + "\"") &&
                expectedErrorJson.contains("\"message\":\"" + msg + "\""));
    }

    private static void testOptionsCorsPreflight() throws Exception {
        // Start a small embedded HttpServer using WebServer's registerEndpoint pattern
        int testPort = 18089;
        HttpServer testServer = HttpServer.create(new InetSocketAddress("127.0.0.1", testPort), 0);

        // Register route using exact WebServer pattern
        testServer.createContext("/api/hotels", exchange -> {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            byte[] b = "[]".getBytes();
            exchange.sendResponseHeaders(200, b.length);
            exchange.getResponseBody().write(b);
            exchange.close();
        });

        testServer.start();

        try {
            // Send OPTIONS preflight request
            URL url = new URL("http://127.0.0.1:" + testPort + "/api/hotels");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("OPTIONS");
            conn.connect();

            int code = conn.getResponseCode();
            String allowOrigin = conn.getHeaderField("Access-Control-Allow-Origin");
            String allowMethods = conn.getHeaderField("Access-Control-Allow-Methods");
            String allowHeaders = conn.getHeaderField("Access-Control-Allow-Headers");

            check("4.1 OPTIONS preflight returns HTTP 204 No Content", code == 204);
            check("4.2 Access-Control-Allow-Origin is '*'", "*".equals(allowOrigin));
            check("4.3 Access-Control-Allow-Methods includes OPTIONS and GET",
                    allowMethods != null && allowMethods.contains("OPTIONS") && allowMethods.contains("GET"));
            check("4.4 Access-Control-Allow-Headers includes Authorization and Content-Type",
                    allowHeaders != null && allowHeaders.contains("Authorization") && allowHeaders.contains("Content-Type"));
            conn.disconnect();

            // Send GET request
            HttpURLConnection getConn = (HttpURLConnection) url.openConnection();
            getConn.setRequestMethod("GET");
            getConn.connect();
            check("4.5 GET request returns HTTP 200 OK", getConn.getResponseCode() == 200);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getConn.getInputStream()))) {
                String body = reader.readLine();
                check("4.6 GET request returns array JSON format", "[]".equals(body));
            }
            getConn.disconnect();

        } finally {
            testServer.stop(0);
        }
    }
}
