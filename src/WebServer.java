import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.*;
import service.*;
import util.DatabaseInitializer;
import util.SessionManager;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Web Server for VoyageQuest Online Travel Booking System.
 * Serves a modern browser-based web application and RESTful JSON APIs
 * using the standard JDK HttpServer (no external dependencies required).
 */
public class WebServer {

    private static final AuthService authService = new AuthService();
    private static final DestinationService destService = new DestinationService();
    private static final PackageService pkgService = new PackageService();
    private static final HotelService hotelService = new HotelService();
    private static final BookingService bookingService = new BookingService();
    private static final PaymentService paymentService = new PaymentService();
    private static final AdminService adminService = new AdminService();

    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        String propPort = System.getProperty("server.port");
        if (propPort != null && !propPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(propPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return 8080;
    }

    public static void main(String[] args) {
        int port = getPort();
        System.out.println("==========================================================");
        System.out.println("      VOYAGEQUEST WEB SERVER: STARTING ON PORT " + port + "      ");
        System.out.println("==========================================================");

        // Ensure database tables and sample data are ready
        DatabaseInitializer.initializeDatabase();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            // 1. Static file handler (SPA Web Interface)
            server.createContext("/", new StaticFileHandler());

            // 2. REST API handlers
            server.createContext("/api/destinations", new DestinationsHandler());
            server.createContext("/api/packages", new PackagesHandler());
            server.createContext("/api/hotels", new HotelsHandler());
            server.createContext("/api/auth/login", new LoginHandler());
            server.createContext("/api/auth/register", new RegisterHandler());
            server.createContext("/api/bookings", new BookingsHandler());
            server.createContext("/api/bookings/cancel", new BookingCancelHandler());
            server.createContext("/api/admin/stats", new AdminStatsHandler());
            server.createContext("/api/admin/users", new AdminUsersHandler());
            server.createContext("/api/admin/toggle-user", new AdminToggleUserHandler());
            server.createContext("/api/admin/bookings", new AdminBookingsHandler());
            server.createContext("/api/admin/update-booking-status", new AdminUpdateBookingStatusHandler());
            server.createContext("/api/admin/payments", new AdminPaymentsHandler());
            server.createContext("/api/admin/destination", new AdminDestinationCrudHandler());
            server.createContext("/api/admin/package", new AdminPackageCrudHandler());
            server.createContext("/api/admin/hotel", new AdminHotelCrudHandler());

            server.start();

            String webUrl = "http://localhost:" + port;
            System.out.println("\n>>> Web Application running successfully!");
            System.out.println(">>> Server listening on 0.0.0.0:" + port);
            System.out.println("==========================================================\n");

            // Open browser only when running in a local desktop environment
            openBrowser(webUrl);

        } catch (IOException e) {
            System.err.println("Error starting web server on port " + port + ": " + e.getMessage());
        }
    }

    private static void openBrowser(String url) {
        // Skip browser launch in headless / cloud environments (Render, Railway, Docker)
        if (System.getenv("PORT") != null || java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            System.out.println("Notice: Visit " + url + " in your browser.");
        }
    }

    // =========================================================================
    // HTTP Handlers & API Endpoints
    // =========================================================================

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists()) {
                file = new File("web/index.html");
            }

            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String contentType = "text/html; charset=utf-8";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".json")) contentType = "application/json";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    static class DestinationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Destination> list = destService.getAllDestinations();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Destination d = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"state\":\"%s\",\"description\":\"%s\",\"attractions\":\"%s\",\"bestTime\":\"%s\"}",
                            d.getId(), escape(d.getName()), escape(d.getState()), escape(d.getDescription()), escape(d.getAttractions()), escape(d.getBestTime())));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class PackagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                String keyword = query.get("keyword");
                Integer destId = query.containsKey("destId") && !query.get("destId").isEmpty() ? Integer.parseInt(query.get("destId")) : null;

                List<TravelPackage> list = pkgService.searchPackages(keyword, destId, null);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    TravelPackage p = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"packageName\":\"%s\",\"destinationId\":%d,\"destinationName\":\"%s\"," +
                                    "\"durationDays\":%d,\"durationNights\":%d,\"pricePerPerson\":%.2f," +
                                    "\"placesCovered\":\"%s\",\"hotelIncluded\":%b,\"foodIncluded\":%b,\"transportIncluded\":%b," +
                                    "\"description\":\"%s\",\"status\":\"%s\"}",
                            p.getId(), escape(p.getPackageName()), p.getDestinationId(), escape(p.getDestinationName()),
                            p.getDurationDays(), p.getDurationNights(), p.getPricePerPerson(),
                            escape(p.getPlacesCovered()), p.isHotelIncluded(), p.isFoodIncluded(), p.isTransportIncluded(),
                            escape(p.getDescription()), p.getStatus()));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class HotelsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                Integer destId = query.containsKey("destId") && !query.get("destId").isEmpty() ? Integer.parseInt(query.get("destId")) : null;

                List<Hotel> list = (destId != null && destId > 0) ? hotelService.getHotelsByDestination(destId) : hotelService.getAllHotels();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Hotel h = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"hotelName\":\"%s\",\"destinationId\":%d,\"destinationName\":\"%s\"," +
                                    "\"address\":\"%s\",\"roomType\":\"%s\",\"pricePerNight\":%.2f," +
                                    "\"availableRooms\":%d,\"rating\":%.1f,\"description\":\"%s\",\"status\":\"%s\"}",
                            h.getId(), escape(h.getHotelName()), h.getDestinationId(), escape(h.getDestinationName()),
                            escape(h.getAddress()), escape(h.getRoomType()), h.getPricePerNight(),
                            h.getAvailableRooms(), h.getRating(), escape(h.getDescription()), h.getStatus()));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                String email = params.get("email");
                String password = params.get("password");

                User user = authService.login(email, password);
                String json = String.format("{\"success\":true,\"user\":{\"id\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}}",
                        user.getId(), escape(user.getFullName()), escape(user.getEmail()), escape(user.getPhone()), user.getRole());
                sendJsonResponse(exchange, 200, json);
            } catch (AuthenticationException e) {
                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                String name = params.get("fullName");
                String email = params.get("email");
                String phone = params.get("phone");
                String password = params.get("password");
                String confirm = params.get("confirmPassword");

                User user = authService.register(name, email, phone, password, confirm);
                String json = String.format("{\"success\":true,\"user\":{\"id\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}}",
                        user.getId(), escape(user.getFullName()), escape(user.getEmail()), escape(user.getPhone()), user.getRole());
                sendJsonResponse(exchange, 200, json);
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class BookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                    int userId = Integer.parseInt(query.getOrDefault("userId", "0"));
                    List<Booking> list = (userId > 0) ? bookingService.getUserBookings(userId) : bookingService.getAllBookings();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Booking b = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"bookingCode\":\"%s\",\"userId\":%d,\"userName\":\"%s\"," +
                                        "\"packageId\":%d,\"packageName\":\"%s\",\"destinationName\":\"%s\"," +
                                        "\"hotelName\":\"%s\",\"travelDate\":\"%s\",\"persons\":%d," +
                                        "\"packageCost\":%.2f,\"hotelCost\":%.2f,\"totalAmount\":%.2f," +
                                        "\"specialRequests\":\"%s\",\"bookingStatus\":\"%s\",\"paymentStatus\":\"%s\"}",
                                b.getId(), escape(b.getBookingCode()), b.getUserId(), escape(b.getUserName()),
                                b.getPackageId(), escape(b.getPackageName()), escape(b.getDestinationName()),
                                escape(b.getHotelName() != null ? b.getHotelName() : "Package Inclusions Only"),
                                b.getTravelDate() != null ? b.getTravelDate().toString() : "",
                                b.getPersons(), b.getPackageCost(), b.getHotelCost(), b.getTotalAmount(),
                                escape(b.getSpecialRequests() != null ? b.getSpecialRequests() : ""),
                                b.getBookingStatus(), b.getPaymentStatus() != null ? b.getPaymentStatus() : "PAID"));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    int userId = Integer.parseInt(p.get("userId"));
                    int pkgId = Integer.parseInt(p.get("packageId"));
                    Integer hotelId = p.containsKey("hotelId") && !p.get("hotelId").isEmpty() && !"null".equalsIgnoreCase(p.get("hotelId"))
                            ? Integer.parseInt(p.get("hotelId")) : null;
                    String travelDate = p.get("travelDate");
                    int persons = Integer.parseInt(p.get("persons"));
                    String requests = p.get("specialRequests");
                    String payMethod = p.getOrDefault("paymentMethod", "UPI");
                    String payDetails = p.getOrDefault("paymentDetails", "Direct simulated payment");

                    Booking booking = bookingService.createBooking(userId, pkgId, hotelId, travelDate, persons, requests);
                    PaymentResult payResult = paymentService.executePayment(booking.getId(), userId, booking.getTotalAmount(), payMethod, payDetails);

                    String json = String.format("{\"success\":true,\"bookingCode\":\"%s\",\"totalAmount\":%.2f,\"txnCode\":\"%s\",\"paymentStatus\":\"%s\",\"bookingId\":%d}",
                            booking.getBookingCode(), booking.getTotalAmount(), payResult.getTransactionCode(), payResult.getStatus(), booking.getId());
                    sendJsonResponse(exchange, 200, json);
                } catch (ValidationException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class BookingCancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                int bookingId = Integer.parseInt(p.get("bookingId"));
                int userId = Integer.parseInt(p.get("userId"));
                boolean isAdmin = Boolean.parseBoolean(p.getOrDefault("isAdmin", "false"));

                boolean ok = bookingService.cancelBooking(bookingId, userId, isAdmin);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                AdminStats s = adminService.getDashboardStats();
                String json = String.format("{\"totalUsers\":%d,\"totalDestinations\":%d,\"totalPackages\":%d,\"totalHotels\":%d," +
                                "\"totalBookings\":%d,\"confirmedBookings\":%d,\"cancelledBookings\":%d,\"totalRevenue\":%.2f}",
                        s.getTotalUsers(), s.getTotalDestinations(), s.getTotalPackages(), s.getTotalHotels(),
                        s.getTotalBookings(), s.getConfirmedBookings(), s.getCancelledBookings(), s.getTotalRevenue());
                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<User> list = adminService.getAllUsers();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    User u = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\",\"status\":\"%s\"}",
                            u.getId(), escape(u.getFullName()), escape(u.getEmail()), escape(u.getPhone()), u.getRole(), u.getStatus()));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminToggleUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                int userId = Integer.parseInt(p.get("userId"));
                String status = p.get("status");
                boolean ok = adminService.toggleUserStatus(userId, status);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Booking> list = adminService.getAllBookings();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Booking b = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"bookingCode\":\"%s\",\"userName\":\"%s\",\"packageName\":\"%s\"," +
                                    "\"destinationName\":\"%s\",\"travelDate\":\"%s\",\"persons\":%d,\"totalAmount\":%.2f,\"bookingStatus\":\"%s\"}",
                            b.getId(), escape(b.getBookingCode()), escape(b.getUserName()), escape(b.getPackageName()),
                            escape(b.getDestinationName()), b.getTravelDate() != null ? b.getTravelDate().toString() : "",
                            b.getPersons(), b.getTotalAmount(), b.getBookingStatus()));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminUpdateBookingStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                int id = Integer.parseInt(p.get("bookingId"));
                String status = p.get("status");
                boolean ok;
                if ("CANCELLED".equalsIgnoreCase(status)) {
                    Booking b = bookingService.getBookingById(id);
                    ok = bookingService.cancelBooking(id, b.getUserId(), true);
                } else {
                    ok = adminService.updateBookingStatus(id, status);
                }
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminPaymentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Payment> list = adminService.getAllPayments();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Payment p = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"transactionCode\":\"%s\",\"bookingCode\":\"%s\",\"userName\":\"%s\"," +
                                    "\"amount\":%.2f,\"paymentMethod\":\"%s\",\"paymentDetails\":\"%s\",\"paymentStatus\":\"%s\"}",
                            p.getId(), escape(p.getTransactionCode()), escape(p.getBookingCode()), escape(p.getUserName()),
                            p.getAmount(), p.getPaymentMethod(), escape(p.getPaymentDetails()), p.getPaymentStatus()));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminDestinationCrudHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                String action = p.get("action");
                if ("delete".equalsIgnoreCase(action)) {
                    int id = Integer.parseInt(p.get("id"));
                    destService.deleteDestination(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("save".equalsIgnoreCase(action)) {
                    String idStr = p.get("id");
                    String name = p.get("name");
                    String state = p.get("state");
                    String desc = p.get("description");
                    String attr = p.get("attractions");
                    String best = p.get("bestTime");
                    if (idStr != null && !idStr.isEmpty() && !"0".equals(idStr)) {
                        destService.updateDestination(Integer.parseInt(idStr), name, state, desc, attr, best);
                    } else {
                        destService.addDestination(name, state, desc, attr, best);
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminPackageCrudHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                String action = p.get("action");
                if ("delete".equalsIgnoreCase(action)) {
                    int id = Integer.parseInt(p.get("id"));
                    pkgService.deletePackage(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("save".equalsIgnoreCase(action)) {
                    String idStr = p.get("id");
                    String name = p.get("packageName");
                    int destId = Integer.parseInt(p.get("destinationId"));
                    int days = Integer.parseInt(p.get("durationDays"));
                    int nights = Integer.parseInt(p.get("durationNights"));
                    double price = Double.parseDouble(p.get("pricePerPerson"));
                    String places = p.get("placesCovered");
                    boolean htl = Boolean.parseBoolean(p.getOrDefault("hotelIncluded", "true"));
                    boolean food = Boolean.parseBoolean(p.getOrDefault("foodIncluded", "true"));
                    boolean trans = Boolean.parseBoolean(p.getOrDefault("transportIncluded", "true"));
                    String desc = p.get("description");
                    String status = p.getOrDefault("status", "ACTIVE");

                    if (idStr != null && !idStr.isEmpty() && !"0".equals(idStr)) {
                        pkgService.updatePackage(Integer.parseInt(idStr), name, destId, days, nights, price, places, htl, food, trans, desc, status);
                    } else {
                        pkgService.addPackage(name, destId, days, nights, price, places, htl, food, trans, desc);
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminHotelCrudHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                String action = p.get("action");
                if ("delete".equalsIgnoreCase(action)) {
                    int id = Integer.parseInt(p.get("id"));
                    hotelService.deleteHotel(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("adjustRooms".equalsIgnoreCase(action)) {
                    int id = Integer.parseInt(p.get("id"));
                    int delta = Integer.parseInt(p.get("delta"));
                    hotelService.updateRoomAvailability(id, delta);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("save".equalsIgnoreCase(action)) {
                    String idStr = p.get("id");
                    String name = p.get("hotelName");
                    int destId = Integer.parseInt(p.get("destinationId"));
                    String addr = p.get("address");
                    String type = p.get("roomType");
                    double price = Double.parseDouble(p.get("pricePerNight"));
                    int rooms = Integer.parseInt(p.get("availableRooms"));
                    double rating = Double.parseDouble(p.getOrDefault("rating", "4.5"));
                    String desc = p.get("description");
                    String status = p.getOrDefault("status", "ACTIVE");

                    if (idStr != null && !idStr.isEmpty() && !"0".equals(idStr)) {
                        hotelService.updateHotel(Integer.parseInt(idStr), name, destId, addr, type, price, rooms, rating, desc, status);
                    } else {
                        hotelService.addHotel(name, destId, addr, type, price, rooms, rating, desc);
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, int status, String msg) throws IOException {
        String json = "{\"success\":false,\"error\":\"" + escape(msg) + "\"}";
        sendJsonResponse(exchange, status, json);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length > 0) {
                try {
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String val = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                    map.put(key, val);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static Map<String, String> parseBody(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString().trim();
        Map<String, String> map = new HashMap<>();

        if (body.startsWith("{") && body.endsWith("}")) {
            // Simple JSON parser
            String inner = body.substring(1, body.length() - 1);
            List<String> tokens = splitJsonTokens(inner);
            for (String token : tokens) {
                String[] kv = token.split(":", 2);
                if (kv.length == 2) {
                    String k = cleanQuotes(kv[0].trim());
                    String v = cleanQuotes(kv[1].trim());
                    map.put(k, v);
                }
            }
        } else {
            // URL Encoded form
            map.putAll(parseQuery(body));
        }
        return map;
    }

    private static List<String> splitJsonTokens(String s) {
        List<String> list = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (c == ',' && !inQuotes) {
                list.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) list.add(cur.toString().trim());
        return list;
    }

    private static String cleanQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
