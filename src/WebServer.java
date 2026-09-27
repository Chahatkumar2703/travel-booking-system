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
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Web Server for Online Travel Booking Platform.
 * Serves modern multi-page web interfaces and RESTful JSON APIs
 * using the standard JDK HttpServer (no external dependencies required).
 */
public class WebServer {

    private static final AuthService authService = new AuthService();
    private static final DestinationService destService = new DestinationService();
    private static final PackageService pkgService = new PackageService();
    private static final HotelService hotelService = new HotelService();
    private static final FlightService flightService = new FlightService();
    private static final CarService carService = new CarService();
    private static final BookingService bookingService = new BookingService();
    private static final PaymentService paymentService = new PaymentService();
    private static final AgentService agentService = new AgentService();
    private static final AdminService adminService = new AdminService();
    private static final MessageService messageService = new MessageService();
    private static final SettingsService settingsService = new SettingsService();

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
        System.out.println("  ONLINE TRAVEL BOOKING PLATFORM - HTTP SERVER ON PORT " + port);
        System.out.println("==========================================================");

        // Ensure database tables, migrations, and sample data are ready
        DatabaseInitializer.initializeDatabase();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            // 1. Static file handler (Multi-page Web Interface)
            server.createContext("/", new StaticFileHandler());

            // 2. Authentication endpoints
            registerEndpoint(server, "/api/auth/login", new LoginHandler());
            registerEndpoint(server, "/api/auth/register", new RegisterHandler());

            // 3. Travel Catalogs
            registerEndpoint(server, "/api/destinations", new DestinationsHandler());
            registerEndpoint(server, "/api/flights", new FlightsHandler());
            registerEndpoint(server, "/api/hotels", new HotelsHandler());
            registerEndpoint(server, "/api/cars", new CarsHandler());
            registerEndpoint(server, "/api/packages", new PackagesHandler());

            // 4. Bookings & Itinerary
            registerEndpoint(server, "/api/bookings", new BookingsHandler());
            registerEndpoint(server, "/api/bookings/cancel", new BookingCancelHandler());
            registerEndpoint(server, "/api/itinerary", new ItineraryHandler());

            // 5. Messages / Feedback
            registerEndpoint(server, "/api/messages", new MessagesHandler());
            registerEndpoint(server, "/api/messages/reply", new MessageReplyHandler());

            // 6. Travel Agent Portal APIs
            registerEndpoint(server, "/api/agent/stats", new AgentStatsHandler());
            registerEndpoint(server, "/api/agent/listings", new AgentListingsHandler());
            registerEndpoint(server, "/api/agent/bookings", new AgentBookingsHandler());

            // 7. Administrator Portal APIs
            registerEndpoint(server, "/api/admin/stats", new AdminStatsHandler());
            registerEndpoint(server, "/api/admin/users", new AdminUsersHandler());
            registerEndpoint(server, "/api/admin/toggle-user", new AdminToggleUserHandler());
            registerEndpoint(server, "/api/admin/listings", new AdminListingsHandler());
            registerEndpoint(server, "/api/admin/bookings", new AdminBookingsHandler());
            registerEndpoint(server, "/api/admin/update-booking-status", new AdminUpdateBookingStatusHandler());
            registerEndpoint(server, "/api/admin/payments", new AdminPaymentsHandler());
            registerEndpoint(server, "/api/admin/settings", new AdminSettingsHandler());
            registerEndpoint(server, "/api/admin/destination", new AdminDestinationCrudHandler());

            server.start();

            String webUrl = "http://localhost:" + port;
            System.out.println("\n>>> Online Travel Booking Platform running successfully!");
            System.out.println(">>> Server listening on 0.0.0.0:" + port);
            System.out.println(">>> Access application in browser at: " + webUrl);
            System.out.println("==========================================================\n");

            openBrowser(webUrl);

        } catch (IOException e) {
            System.err.println("Error starting web server on port " + port + ": " + e.getMessage());
        }
    }

    private static void openBrowser(String url) {
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

    private static void registerEndpoint(HttpServer server, String path, HttpHandler handler) {
        server.createContext(path, exchange -> {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            handler.handle(exchange);
        });
    }

    // =========================================================================
    // 1. Static File Handler (Resolves frontend/ and web/ directories)
    // =========================================================================

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token");
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }

            File file = resolveFile(path);
            if (file == null || !file.exists() || file.isDirectory()) {
                // If requesting without .html extension, try adding .html
                if (!path.contains(".")) {
                    file = resolveFile(path + ".html");
                }
            }

            if (file == null || !file.exists() || file.isDirectory()) {
                file = resolveFile("/index.html");
            }

            if (file != null && file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String filePath = file.getName().toLowerCase();
                String contentType = "text/html; charset=utf-8";
                if (filePath.endsWith(".css")) contentType = "text/css; charset=utf-8";
                else if (filePath.endsWith(".js")) contentType = "application/javascript; charset=utf-8";
                else if (filePath.endsWith(".json")) contentType = "application/json; charset=utf-8";
                else if (filePath.endsWith(".png")) contentType = "image/png";
                else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (filePath.endsWith(".svg")) contentType = "image/svg+xml";

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

        private File resolveFile(String relativePath) {
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
            // 1. Check frontend/
            File f1 = new File("frontend/" + relativePath);
            if (f1.exists()) return f1;
            // 2. Check web/
            File f2 = new File("web/" + relativePath);
            if (f2.exists()) return f2;
            return null;
        }
    }

    // =========================================================================
    // 2. Authentication Handlers
    // =========================================================================

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
                String token = SessionManager.createWebSession(user);
                exchange.getResponseHeaders().set("Set-Cookie", "session_token=" + token + "; Path=/; SameSite=Lax; Max-Age=86400");
                String json = String.format("{\"success\":true,\"token\":\"%s\",\"user\":{\"id\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\",\"token\":\"%s\"}}",
                        token, user.getId(), escape(user.getFullName()), escape(user.getEmail()), escape(user.getPhone()), user.getRole(), token);
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
                String role = params.getOrDefault("role", "TRAVELER");

                User user = authService.register(name, email, phone, password, confirm, role);
                String token = SessionManager.createWebSession(user);
                exchange.getResponseHeaders().set("Set-Cookie", "session_token=" + token + "; Path=/; SameSite=Lax; Max-Age=86400");
                String json = String.format("{\"success\":true,\"token\":\"%s\",\"user\":{\"id\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\",\"token\":\"%s\"}}",
                        token, user.getId(), escape(user.getFullName()), escape(user.getEmail()), escape(user.getPhone()), user.getRole(), token);
                sendJsonResponse(exchange, 200, json);
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    // =========================================================================
    // 3. Travel Catalogs Handlers (Flights, Hotels, Cars, Packages, Destinations)
    // =========================================================================

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

    static class FlightsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                    String origin = q.get("origin");
                    String destination = q.get("destination");
                    String date = q.get("date");
                    String airline = q.get("airline");
                    Double maxPrice = q.containsKey("maxPrice") && !q.get("maxPrice").isEmpty() ? Double.parseDouble(q.get("maxPrice")) : null;

                    List<Flight> list = flightService.searchFlights(origin, destination, date, maxPrice, airline);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Flight f = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"agentId\":%d,\"agentName\":\"%s\",\"airline\":\"%s\",\"flightNumber\":\"%s\"," +
                                        "\"origin\":\"%s\",\"destination\":\"%s\",\"departureDate\":\"%s\",\"departureTime\":\"%s\"," +
                                        "\"arrivalTime\":\"%s\",\"price\":%.2f,\"availableSeats\":%d,\"imageUrl\":\"%s\",\"approvalStatus\":\"%s\"}",
                                f.getId(), f.getAgentId(), escape(f.getAgentName()), escape(f.getAirline()), escape(f.getFlightNumber()),
                                escape(f.getOrigin()), escape(f.getDestination()),
                                f.getDepartureDate() != null ? f.getDepartureDate().toString() : "",
                                escape(f.getDepartureTime()), escape(f.getArrivalTime()),
                                f.getPrice(), f.getAvailableSeats(), escape(f.getImageUrl()), f.getApprovalStatus()));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Add flight
                try {
                    User authUser = SessionManager.authenticateRequest(exchange);
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    int agentId;
                    if (authUser != null) {
                        if (authUser.isAgent()) {
                            agentId = authUser.getId();
                        } else if (authUser.isAdmin()) {
                            agentId = Integer.parseInt(p.getOrDefault("agentId", String.valueOf(authUser.getId())));
                        } else {
                            sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"Access denied. Only Travel Agents can create flight listings.\"}");
                            return;
                        }
                    } else {
                        agentId = Integer.parseInt(p.getOrDefault("agentId", "2"));
                    }
                    String airline = p.get("airline");
                    String flightNumber = p.get("flightNumber");
                    String origin = p.get("origin");
                    String destination = p.get("destination");
                    String depDate = p.get("departureDate");
                    String depTime = p.get("departureTime");
                    String arrTime = p.get("arrivalTime");
                    double price = Double.parseDouble(p.get("price"));
                    int seats = Integer.parseInt(p.getOrDefault("availableSeats", "60"));
                    String img = p.get("imageUrl");

                    Flight f = flightService.addFlight(agentId, airline, flightNumber, origin, destination, depDate, depTime, arrTime, price, seats, img);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + f.getId() + ",\"approvalStatus\":\"" + f.getApprovalStatus() + "\",\"message\":\"Flight submitted for admin approval.\"}");
                } catch (ValidationException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class HotelsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                    String location = q.get("location");
                    Double maxPrice = q.containsKey("maxPrice") && !q.get("maxPrice").isEmpty() ? Double.parseDouble(q.get("maxPrice")) : null;
                    Double minRating = q.containsKey("rating") && !q.get("rating").isEmpty() ? Double.parseDouble(q.get("rating")) : null;
                    Integer destId = q.containsKey("destId") && !q.get("destId").isEmpty() ? Integer.parseInt(q.get("destId")) : null;

                    // Public catalog only displays APPROVED hotels
                    List<Hotel> list = hotelService.getApprovedHotels(location, maxPrice, minRating, destId);

                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Hotel h = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"agentId\":%d,\"agentName\":\"%s\",\"hotelName\":\"%s\",\"destinationId\":%d," +
                                        "\"destinationName\":\"%s\",\"address\":\"%s\",\"location\":\"%s\",\"roomType\":\"%s\"," +
                                        "\"pricePerNight\":%.2f,\"availableRooms\":%d,\"rating\":%.1f,\"description\":\"%s\",\"imageUrl\":\"%s\",\"approvalStatus\":\"%s\"}",
                                h.getId(), h.getAgentId(), escape(h.getAgentName()), escape(h.getHotelName()), h.getDestinationId(),
                                escape(h.getDestinationName()), escape(h.getAddress()), escape(h.getLocation()), escape(h.getRoomType()),
                                h.getPricePerNight(), h.getAvailableRooms(), h.getRating(), escape(h.getDescription()), escape(h.getImageUrl()), h.getApprovalStatus()));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Add hotel
                try {
                    User authUser = SessionManager.authenticateRequest(exchange);
                    Map<String, String> p = parseBody(exchange.getRequestBody());

                    int agentId;
                    if (authUser != null) {
                        if (authUser.isAgent()) {
                            // agent_id MUST come from the authenticated logged-in Agent session.
                            // Any agentId in the JSON body is ignored to prevent creating on behalf of another agent.
                            agentId = authUser.getId();
                        } else if (authUser.isAdmin()) {
                            agentId = Integer.parseInt(p.getOrDefault("agentId", String.valueOf(authUser.getId())));
                        } else {
                            sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"Access denied. Only Travel Agents can create hotel listings.\"}");
                            return;
                        }
                    } else {
                        agentId = Integer.parseInt(p.getOrDefault("agentId", "2"));
                    }

                    String hotelName = p.get("hotelName");
                    String location = p.get("location");
                    String address = p.getOrDefault("address", location);
                    String roomType = p.getOrDefault("roomType", "Deluxe");

                    Integer destId = null;
                    if (p.containsKey("destinationId") && !p.get("destinationId").trim().isEmpty()) {
                        try {
                            destId = Integer.parseInt(p.get("destinationId").trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    double price = 0.0;
                    if (p.containsKey("pricePerNight") && !p.get("pricePerNight").trim().isEmpty()) {
                        price = Double.parseDouble(p.get("pricePerNight").trim());
                    } else if (p.containsKey("price") && !p.get("price").trim().isEmpty()) {
                        price = Double.parseDouble(p.get("price").trim());
                    }

                    int rooms = 10;
                    if (p.containsKey("availableRooms") && !p.get("availableRooms").trim().isEmpty()) {
                        rooms = Integer.parseInt(p.get("availableRooms").trim());
                    } else if (p.containsKey("rooms") && !p.get("rooms").trim().isEmpty()) {
                        rooms = Integer.parseInt(p.get("rooms").trim());
                    }

                    double rating = 4.5;
                    if (p.containsKey("rating") && !p.get("rating").trim().isEmpty()) {
                        try { rating = Double.parseDouble(p.get("rating").trim()); } catch (NumberFormatException ignored) {}
                    }

                    String desc = p.get("description");
                    if (desc == null || desc.trim().isEmpty()) {
                        desc = p.get("amenities");
                    }
                    String img = p.getOrDefault("imageUrl", "");

                    Hotel h = hotelService.addHotel(agentId, hotelName, location, address, destId, roomType, price, rooms, rating, desc, img);

                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + h.getId() + ",\"approvalStatus\":\"" + h.getApprovalStatus() + "\",\"message\":\"Hotel listing submitted for admin approval.\"}");
                } catch (ValidationException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class CarsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                    String location = q.get("location");
                    String carType = q.get("carType");
                    Double maxPrice = q.containsKey("maxPrice") && !q.get("maxPrice").isEmpty() ? Double.parseDouble(q.get("maxPrice")) : null;

                    List<Car> list = carService.searchCars(location, carType, maxPrice);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Car c = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"agentId\":%d,\"agentName\":\"%s\",\"carName\":\"%s\",\"brand\":\"%s\"," +
                                        "\"model\":\"%s\",\"location\":\"%s\",\"carType\":\"%s\",\"pricePerDay\":%.2f," +
                                        "\"availableUnits\":%d,\"imageUrl\":\"%s\",\"approvalStatus\":\"%s\"}",
                                c.getId(), c.getAgentId(), escape(c.getAgentName()), escape(c.getCarName()), escape(c.getBrand()),
                                escape(c.getModel()), escape(c.getLocation()), escape(c.getCarType()), c.getPricePerDay(),
                                c.getAvailableUnits(), escape(c.getImageUrl()), c.getApprovalStatus()));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Add car
                try {
                    User authUser = SessionManager.authenticateRequest(exchange);
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    int agentId;
                    if (authUser != null) {
                        if (authUser.isAgent()) {
                            agentId = authUser.getId();
                        } else if (authUser.isAdmin()) {
                            agentId = Integer.parseInt(p.getOrDefault("agentId", String.valueOf(authUser.getId())));
                        } else {
                            sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"Access denied. Only Travel Agents can create rental car listings.\"}");
                            return;
                        }
                    } else {
                        agentId = Integer.parseInt(p.getOrDefault("agentId", "2"));
                    }
                    String carName = p.get("carName");
                    String brand = p.get("brand");
                    String modelStr = p.get("model");
                    String location = p.get("location");
                    String carType = p.get("carType");
                    double price = Double.parseDouble(p.get("pricePerDay"));
                    int units = Integer.parseInt(p.getOrDefault("availableUnits", "5"));
                    String img = p.get("imageUrl");

                    Car c = carService.addCar(agentId, carName, brand, modelStr, location, carType, price, units, img);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + c.getId() + ",\"approvalStatus\":\"" + c.getApprovalStatus() + "\",\"message\":\"Car rental listing submitted for admin approval.\"}");
                } catch (ValidationException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class PackagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                    String keyword = query.get("keyword");
                    Integer destId = query.containsKey("destId") && !query.get("destId").isEmpty() ? Integer.parseInt(query.get("destId")) : null;
                    Double maxPrice = query.containsKey("maxPrice") && !query.get("maxPrice").isEmpty() ? Double.parseDouble(query.get("maxPrice")) : null;

                    List<TravelPackage> list = pkgService.searchPackages(keyword, destId, maxPrice);
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        TravelPackage p = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"agentId\":%d,\"agentName\":\"%s\",\"packageName\":\"%s\",\"destinationId\":%d," +
                                        "\"destinationName\":\"%s\",\"durationDays\":%d,\"durationNights\":%d,\"pricePerPerson\":%.2f," +
                                        "\"placesCovered\":\"%s\",\"hotelIncluded\":%b,\"foodIncluded\":%b,\"transportIncluded\":%b," +
                                        "\"description\":\"%s\",\"imageUrl\":\"%s\",\"approvalStatus\":\"%s\"}",
                                p.getId(), p.getAgentId(), escape(p.getAgentName()), escape(p.getPackageName()), p.getDestinationId(),
                                escape(p.getDestinationName()), p.getDurationDays(), p.getDurationNights(), p.getPricePerPerson(),
                                escape(p.getPlacesCovered()), p.isHotelIncluded(), p.isFoodIncluded(), p.isTransportIncluded(),
                                escape(p.getDescription()), escape(p.getImageUrl()), p.getApprovalStatus()));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Add package
                try {
                    User authUser = SessionManager.authenticateRequest(exchange);
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    int agentId;
                    if (authUser != null) {
                        if (authUser.isAgent()) {
                            agentId = authUser.getId();
                        } else if (authUser.isAdmin()) {
                            agentId = Integer.parseInt(p.getOrDefault("agentId", String.valueOf(authUser.getId())));
                        } else {
                            sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"Access denied. Only Travel Agents can create package listings.\"}");
                            return;
                        }
                    } else {
                        agentId = Integer.parseInt(p.getOrDefault("agentId", "2"));
                    }
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
                    String img = p.get("imageUrl");

                    TravelPackage pkg = new TravelPackage();
                    pkg.setAgentId(agentId);
                    pkg.setPackageName(name);
                    pkg.setDestinationId(destId);
                    pkg.setDurationDays(days);
                    pkg.setDurationNights(nights);
                    pkg.setPricePerPerson(price);
                    pkg.setPlacesCovered(places);
                    pkg.setHotelIncluded(htl);
                    pkg.setFoodIncluded(food);
                    pkg.setTransportIncluded(trans);
                    pkg.setDescription(desc);
                    pkg.setImageUrl(img);
                    pkg.setApprovalStatus("PENDING");
                    new dao.PackageDAO().save(pkg);

                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + pkg.getId() + ",\"message\":\"Package submitted for admin approval.\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    // =========================================================================
    // 4. Universal Bookings & Itinerary Handlers
    // =========================================================================

    static class BookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            User authUser = SessionManager.authenticateRequest(exchange);
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                    int reqUserId = Integer.parseInt(query.getOrDefault("userId", "0"));
                    int reqAgentId = Integer.parseInt(query.getOrDefault("agentId", "0"));

                    List<Booking> list;
                    if (authUser != null) {
                        if (authUser.isTraveler()) {
                            // A traveler can ONLY see their own bookings! Never someone else's.
                            list = bookingService.getUserBookings(authUser.getId());
                        } else if (authUser.isAgent()) {
                            // An agent sees bookings for their own inventory
                            list = agentService.getAgentBookings(authUser.getId());
                        } else if (authUser.isAdmin()) {
                            // Admin can view all or filter by requested user/agent
                            if (reqUserId > 0) {
                                list = bookingService.getUserBookings(reqUserId);
                            } else if (reqAgentId > 0) {
                                list = agentService.getAgentBookings(reqAgentId);
                            } else {
                                list = bookingService.getAllBookings();
                            }
                        } else {
                            list = new ArrayList<>();
                        }
                    } else {
                        // Unauthenticated request (e.g. testing or explicit script query)
                        if (reqUserId > 0) {
                            list = bookingService.getUserBookings(reqUserId);
                        } else if (reqAgentId > 0) {
                            list = agentService.getAgentBookings(reqAgentId);
                        } else {
                            // CRITICAL FIX: NEVER return all bookings when unauthenticated!
                            list = new ArrayList<>();
                        }
                    }

                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Booking b = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"bookingCode\":\"%s\",\"userId\":%d,\"userName\":\"%s\",\"userEmail\":\"%s\"," +
                                        "\"bookingType\":\"%s\",\"itemName\":\"%s\",\"packageId\":%d,\"flightId\":%s,\"hotelId\":%s,\"carId\":%s," +
                                        "\"travelDate\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"persons\":%d,\"quantity\":%d," +
                                        "\"packageCost\":%.2f,\"hotelCost\":%.2f,\"totalAmount\":%.2f,\"specialRequests\":\"%s\"," +
                                        "\"bookingStatus\":\"%s\",\"paymentStatus\":\"%s\"}",
                                b.getId(), escape(b.getBookingCode()), b.getUserId(), escape(b.getUserName()), escape(b.getUserEmail()),
                                b.getBookingType(), escape(b.getItemName()), b.getPackageId(),
                                b.getFlightId() != null ? b.getFlightId().toString() : "null",
                                b.getHotelId() != null ? b.getHotelId().toString() : "null",
                                b.getCarId() != null ? b.getCarId().toString() : "null",
                                b.getTravelDate() != null ? b.getTravelDate().toString() : "",
                                b.getStartDate() != null ? b.getStartDate().toString() : "",
                                b.getEndDate() != null ? b.getEndDate().toString() : "",
                                b.getPersons(), b.getQuantity(), b.getPackageCost(), b.getHotelCost(), b.getTotalAmount(),
                                escape(b.getSpecialRequests() != null ? b.getSpecialRequests() : ""),
                                b.getBookingStatus(), b.getPaymentStatus() != null ? b.getPaymentStatus() : "SUCCESS"));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Universal Booking Creation
                try {
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    int userId;
                    if (authUser != null && !authUser.isAdmin()) {
                        // Strictly bind new booking to the authenticated traveler!
                        userId = authUser.getId();
                    } else {
                        userId = Integer.parseInt(p.getOrDefault("userId", "0"));
                    }
                    if (userId <= 0) {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Valid user authentication required to book.\"}");
                        return;
                    }

                    String type = p.getOrDefault("bookingType", "PACKAGE").toUpperCase();
                    String requests = p.getOrDefault("specialRequests", "");
                    String payMethod = p.getOrDefault("paymentMethod", "UPI");
                    String payDetails = p.getOrDefault("paymentDetails", "Simulated Direct Payment");

                    Booking booking;
                    if ("FLIGHT".equalsIgnoreCase(type)) {
                        int flightId = Integer.parseInt(p.get("flightId"));
                        int passengers = Integer.parseInt(p.getOrDefault("passengers", "1"));
                        String date = p.get("travelDate");
                        booking = bookingService.createFlightBooking(userId, flightId, date, passengers, requests);
                    } else if ("HOTEL".equalsIgnoreCase(type)) {
                        int hotelId = Integer.parseInt(p.get("hotelId"));
                        String checkIn = p.get("checkInDate");
                        String checkOut = p.get("checkOutDate");
                        int rooms = Integer.parseInt(p.getOrDefault("rooms", "1"));
                        int guests = Integer.parseInt(p.getOrDefault("guests", "2"));
                        booking = bookingService.createHotelBooking(userId, hotelId, checkIn, checkOut, rooms, guests, requests);
                    } else if ("CAR".equalsIgnoreCase(type)) {
                        int carId = Integer.parseInt(p.get("carId"));
                        String pickup = p.get("pickupDate");
                        String drop = p.get("dropoffDate");
                        int units = Integer.parseInt(p.getOrDefault("units", "1"));
                        booking = bookingService.createCarBooking(userId, carId, pickup, drop, units, requests);
                    } else {
                        // Package
                        int pkgId = Integer.parseInt(p.get("packageId"));
                        Integer hotelId = p.containsKey("hotelId") && !p.get("hotelId").isEmpty() && !"null".equalsIgnoreCase(p.get("hotelId"))
                                ? Integer.parseInt(p.get("hotelId")) : null;
                        String travelDate = p.get("travelDate");
                        int persons = Integer.parseInt(p.getOrDefault("persons", "1"));
                        booking = bookingService.createBooking(userId, pkgId, hotelId, travelDate, persons, requests);
                    }

                    PaymentResult payResult = paymentService.executePayment(booking.getId(), userId, booking.getTotalAmount(), payMethod, payDetails);

                    String json = String.format("{\"success\":true,\"bookingCode\":\"%s\",\"totalAmount\":%.2f,\"txnCode\":\"%s\",\"paymentStatus\":\"%s\",\"bookingId\":%d,\"bookingType\":\"%s\"}",
                            booking.getBookingCode(), booking.getTotalAmount(), payResult.getTransactionCode(), payResult.getStatus(), booking.getId(), booking.getBookingType());
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
                User authUser = SessionManager.authenticateRequest(exchange);
                Map<String, String> p = parseBody(exchange.getRequestBody());
                int bookingId = Integer.parseInt(p.get("bookingId"));
                int userId = (authUser != null && !authUser.isAdmin()) ? authUser.getId() : Integer.parseInt(p.getOrDefault("userId", "0"));
                boolean isAdmin = (authUser != null && authUser.isAdmin()) || Boolean.parseBoolean(p.getOrDefault("isAdmin", "false"));

                boolean ok = bookingService.cancelBooking(bookingId, userId, isAdmin);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class ItineraryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                User authUser = SessionManager.authenticateRequest(exchange);
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int userId;
                if (authUser != null && !authUser.isAdmin()) {
                    userId = authUser.getId();
                } else {
                    userId = Integer.parseInt(q.getOrDefault("userId", "0"));
                }
                if (userId <= 0) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"totalSegments\":0,\"segments\":[]}");
                    return;
                }

                Map<String, Object> itinerary = bookingService.getItinerary(userId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> segments = (List<Map<String, Object>>) itinerary.get("segments");
                if (segments == null) segments = new ArrayList<>();

                StringBuilder json = new StringBuilder("{\"success\":true,\"totalSegments\":" + segments.size() + ",\"segments\":[");
                for (int i = 0; i < segments.size(); i++) {
                    Map<String, Object> seg = segments.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"bookingId\":%d,\"bookingCode\":\"%s\",\"type\":\"%s\",\"title\":\"%s\"," +
                                    "\"travelDate\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"quantity\":%d,\"amount\":%.2f,\"status\":\"%s\",\"specialRequests\":\"%s\"}",
                            seg.get("bookingId"), escape((String) seg.get("bookingCode")), seg.get("type"), escape((String) seg.get("title")),
                            seg.get("travelDate"), seg.get("startDate"), seg.get("endDate"),
                            seg.get("quantity"), (Double) seg.get("amount"), seg.get("status"), escape((String) seg.get("specialRequests"))));
                }
                json.append("]}");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    // =========================================================================
    // 5. Messages / Feedback Handlers
    // =========================================================================

    static class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            User authUser = SessionManager.authenticateRequest(exchange);
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                    List<Message> list;
                    if (authUser != null) {
                        if (authUser.isTraveler()) {
                            list = messageService.getUserMessages(authUser.getId());
                        } else if (authUser.isAgent()) {
                            list = messageService.getAgentMessages(authUser.getId());
                        } else if (authUser.isAdmin()) {
                            list = messageService.getAllMessages();
                        } else {
                            list = new ArrayList<>();
                        }
                    } else {
                        if (q.containsKey("userId")) {
                            list = messageService.getUserMessages(Integer.parseInt(q.get("userId")));
                        } else if (q.containsKey("agentId")) {
                            list = messageService.getAgentMessages(Integer.parseInt(q.get("agentId")));
                        } else {
                            list = new ArrayList<>();
                        }
                    }

                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Message m = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"userId\":%d,\"userName\":\"%s\",\"userEmail\":\"%s\"," +
                                        "\"agentId\":%s,\"agentName\":\"%s\",\"subject\":\"%s\",\"message\":\"%s\"," +
                                        "\"reply\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\"}",
                                m.getId(), m.getUserId(), escape(m.getUserName()), escape(m.getUserEmail()),
                                m.getAgentId() != null ? m.getAgentId().toString() : "null",
                                escape(m.getAgentName() != null ? m.getAgentName() : "General / Support"),
                                escape(m.getSubject()), escape(m.getMessage()),
                                escape(m.getReply() != null ? m.getReply() : ""), m.getStatus(),
                                m.getCreatedAt() != null ? m.getCreatedAt().toString() : ""));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    int userId = (authUser != null && !authUser.isAdmin()) ? authUser.getId() : Integer.parseInt(p.getOrDefault("userId", "0"));
                    if (userId <= 0) {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Valid user authentication required to send inquiries.\"}");
                        return;
                    }
                    Integer agentId = p.containsKey("agentId") && !p.get("agentId").isEmpty() && !"null".equalsIgnoreCase(p.get("agentId"))
                            ? Integer.parseInt(p.get("agentId")) : null;
                    String subject = p.get("subject");
                    String msgText = p.get("message");

                    Message m = messageService.sendMessage(userId, agentId, subject, msgText);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + m.getId() + ",\"message\":\"Your message has been sent successfully.\"}");
                } catch (ValidationException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class MessageReplyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                Map<String, String> p = parseBody(exchange.getRequestBody());
                int msgId = Integer.parseInt(p.get("messageId"));
                String reply = p.get("reply");

                boolean ok = messageService.replyMessage(msgId, reply);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (ValidationException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    // =========================================================================
    // 6. Travel Agent Handlers
    // =========================================================================

    static class AgentStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                User authUser = SessionManager.authenticateRequest(exchange);
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int agentId = (authUser != null && authUser.isAgent()) ? authUser.getId() : Integer.parseInt(q.getOrDefault("agentId", "2"));
                Map<String, Object> stats = agentService.getAgentStats(agentId);

                String json = String.format("{\"totalListings\":%s,\"totalFlights\":%s,\"totalHotels\":%s,\"totalCars\":%s," +
                                "\"totalPackages\":%s,\"pendingApprovals\":%s,\"totalBookings\":%s,\"confirmedBookings\":%s,\"totalRevenue\":%.2f}",
                        stats.get("totalListings"), stats.get("totalFlights"), stats.get("totalHotels"), stats.get("totalCars"),
                        stats.get("totalPackages"), stats.get("pendingApprovals"), stats.get("totalBookings"), stats.get("confirmedBookings"),
                        (Double) stats.get("totalRevenue"));
                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AgentListingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            User authUser = SessionManager.authenticateRequest(exchange);
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                    int agentId = (authUser != null && authUser.isAgent()) ? authUser.getId() : Integer.parseInt(q.getOrDefault("agentId", "2"));

                    List<Flight> flights = agentService.getAgentFlights(agentId);
                    List<Hotel> hotels = agentService.getAgentHotels(agentId);
                    List<Car> cars = agentService.getAgentCars(agentId);
                    List<TravelPackage> pkgs = agentService.getAgentPackages(agentId);

                    StringBuilder json = new StringBuilder("{\"flights\":[");
                    for (int i = 0; i < flights.size(); i++) {
                        Flight f = flights.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"airline\":\"%s\",\"flightNumber\":\"%s\",\"origin\":\"%s\",\"destination\":\"%s\"," +
                                        "\"departureDate\":\"%s\",\"price\":%.2f,\"seats\":%d,\"approvalStatus\":\"%s\"}",
                                f.getId(), escape(f.getAirline()), escape(f.getFlightNumber()), escape(f.getOrigin()), escape(f.getDestination()),
                                f.getDepartureDate() != null ? f.getDepartureDate().toString() : "", f.getPrice(), f.getAvailableSeats(), f.getApprovalStatus()));
                    }
                    json.append("],\"hotels\":[");
                    for (int i = 0; i < hotels.size(); i++) {
                        Hotel h = hotels.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"hotelName\":\"%s\",\"location\":\"%s\",\"roomType\":\"%s\"," +
                                        "\"pricePerNight\":%.2f,\"availableRooms\":%d,\"approvalStatus\":\"%s\"}",
                                h.getId(), escape(h.getHotelName()), escape(h.getLocation()), escape(h.getRoomType()),
                                h.getPricePerNight(), h.getAvailableRooms(), h.getApprovalStatus()));
                    }
                    json.append("],\"cars\":[");
                    for (int i = 0; i < cars.size(); i++) {
                        Car c = cars.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"carName\":\"%s\",\"brand\":\"%s\",\"location\":\"%s\",\"carType\":\"%s\"," +
                                        "\"pricePerDay\":%.2f,\"availableUnits\":%d,\"approvalStatus\":\"%s\"}",
                                c.getId(), escape(c.getCarName()), escape(c.getBrand()), escape(c.getLocation()), escape(c.getCarType()),
                                c.getPricePerDay(), c.getAvailableUnits(), c.getApprovalStatus()));
                    }
                    json.append("],\"packages\":[");
                    for (int i = 0; i < pkgs.size(); i++) {
                        TravelPackage p = pkgs.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"packageName\":\"%s\",\"durationDays\":%d,\"pricePerPerson\":%.2f,\"approvalStatus\":\"%s\"}",
                                p.getId(), escape(p.getPackageName()), p.getDurationDays(), p.getPricePerPerson(), p.getApprovalStatus()));
                    }
                    json.append("]}");

                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Delete own listing
                try {
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    String action = p.get("action");
                    String type = p.get("type");
                    int id = Integer.parseInt(p.get("id"));

                    if ("delete".equalsIgnoreCase(action)) {
                        boolean ok = adminService.deleteListing(type, id);
                        sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Unknown action\"}");
                    }
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class AgentBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                User authUser = SessionManager.authenticateRequest(exchange);
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int agentId = (authUser != null && authUser.isAgent()) ? authUser.getId() : Integer.parseInt(q.getOrDefault("agentId", "2"));
                List<Booking> list = agentService.getAgentBookings(agentId);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Booking b = list.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"id\":%d,\"bookingCode\":\"%s\",\"userName\":\"%s\",\"userEmail\":\"%s\"," +
                                    "\"bookingType\":\"%s\",\"itemName\":\"%s\",\"travelDate\":\"%s\",\"totalAmount\":%.2f,\"bookingStatus\":\"%s\"}",
                            b.getId(), escape(b.getBookingCode()), escape(b.getUserName()), escape(b.getUserEmail()),
                            b.getBookingType(), escape(b.getItemName()),
                            b.getTravelDate() != null ? b.getTravelDate().toString() : "",
                            b.getTotalAmount(), b.getBookingStatus()));
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    // =========================================================================
    // 7. Administrator Handlers
    // =========================================================================

    static class AdminStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                AdminStats s = adminService.getDashboardStats();
                String json = String.format("{\"totalUsers\":%d,\"totalTravelers\":%d,\"totalAgents\":%d," +
                                "\"totalDestinations\":%d,\"totalPackages\":%d,\"totalHotels\":%d,\"totalFlights\":%d,\"totalCars\":%d," +
                                "\"pendingApprovals\":%d,\"totalBookings\":%d,\"confirmedBookings\":%d,\"cancelledBookings\":%d," +
                                "\"flightBookings\":%d,\"hotelBookings\":%d,\"carBookings\":%d,\"packageBookings\":%d,\"totalRevenue\":%.2f}",
                        s.getTotalUsers(), s.getTotalTravelers(), s.getTotalAgents(),
                        s.getTotalDestinations(), s.getTotalPackages(), s.getTotalHotels(), s.getTotalFlights(), s.getTotalCars(),
                        s.getPendingApprovals(), s.getTotalBookings(), s.getConfirmedBookings(), s.getCancelledBookings(),
                        s.getFlightBookings(), s.getHotelBookings(), s.getCarBookings(), s.getPackageBookings(), s.getTotalRevenue());
                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    static class AdminListingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<Map<String, Object>> list = adminService.getAllListings();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> m = list.get(i);
                        if (i > 0) json.append(",");
                        json.append(String.format("{\"id\":%d,\"type\":\"%s\",\"name\":\"%s\",\"agentId\":%d,\"agentName\":\"%s\",\"price\":%.2f,\"status\":\"%s\",\"approvalStatus\":\"%s\"}",
                                m.get("id"), m.get("type"), escape((String) m.get("name")), m.get("agentId"), escape((String) m.get("agentName")),
                                (Double) m.get("price"), m.get("status"), m.get("approvalStatus")));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Approve, Reject, or Delete listing
                try {
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    String action = p.get("action");
                    String type = p.get("type");
                    int id = Integer.parseInt(p.get("id"));

                    boolean ok;
                    if ("approve".equalsIgnoreCase(action)) {
                        ok = adminService.approveListing(type, id);
                    } else if ("reject".equalsIgnoreCase(action)) {
                        ok = adminService.rejectListing(type, id);
                    } else if ("delete".equalsIgnoreCase(action)) {
                        ok = adminService.deleteListing(type, id);
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Unknown action\"}");
                        return;
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            }
        }
    }

    static class AdminSettingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> map = settingsService.getAllSettings();
                    StringBuilder json = new StringBuilder("{");
                    int i = 0;
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (i++ > 0) json.append(",");
                        json.append(String.format("\"%s\":\"%s\"", escape(entry.getKey()), escape(entry.getValue())));
                    }
                    json.append("}");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> p = parseBody(exchange.getRequestBody());
                    for (Map.Entry<String, String> entry : p.entrySet()) {
                        settingsService.updateSetting(entry.getKey(), entry.getValue());
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Settings updated successfully!\"}");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, e.getMessage());
                }
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
                    json.append(String.format("{\"id\":%d,\"bookingCode\":\"%s\",\"userName\":\"%s\",\"userEmail\":\"%s\"," +
                                    "\"bookingType\":\"%s\",\"itemName\":\"%s\",\"travelDate\":\"%s\",\"persons\":%d," +
                                    "\"totalAmount\":%.2f,\"bookingStatus\":\"%s\"}",
                            b.getId(), escape(b.getBookingCode()), escape(b.getUserName()), escape(b.getUserEmail()),
                            b.getBookingType(), escape(b.getItemName()),
                            b.getTravelDate() != null ? b.getTravelDate().toString() : "",
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

    // =========================================================================
    // Utilities
    // =========================================================================

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, int status, String msg) throws IOException {
        String json = "{\"success\":false,\"error\":\"" + escape(msg) + "\",\"message\":\"" + escape(msg) + "\"}";
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
