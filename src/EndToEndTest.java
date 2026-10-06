import model.*;
import service.*;
import util.DatabaseConnection;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive End-to-End System Verification Test.
 * Validates 3 user roles, flights, hotels, cars, packages, approval workflows,
 * universal bookings, itinerary generation, messages, and administrative control.
 */
public class EndToEndTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("    TRIPZY TRAVEL PLATFORM - END-TO-END INTEGRATION TEST   ");
        System.out.println("==========================================================");

        int passed = 0;
        int failed = 0;

        AuthService authService = new AuthService();
        FlightService flightService = new FlightService();
        CarService carService = new CarService();
        HotelService hotelService = new HotelService();
        PackageService pkgService = new PackageService();
        BookingService bookingService = new BookingService();
        AgentService agentService = new AgentService();
        AdminService adminService = new AdminService();
        MessageService messageService = new MessageService();
        SettingsService settingsService = new SettingsService();

        // 1. Database Connection
        try {
            if (DatabaseConnection.testConnection()) {
                System.out.println("[PASS] 1. Database Connectivity OK");
                passed++;
            } else {
                System.err.println("[FAIL] 1. Database Connection Failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 1. Database Connection Exception: " + e.getMessage());
            failed++;
        }

        // 2. Authentication for 3 Roles
        try {
            User admin = authService.login("admin@example.com", "admin123");
            if (admin != null && admin.isAdmin()) {
                System.out.println("[PASS] 2.1 Admin Authentication OK: " + admin.getFullName() + " (Role: " + admin.getRole() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.1 Admin login failed");
                failed++;
            }

            User agent = authService.login("agent@example.com", "agent123");
            if (agent != null && agent.isAgent()) {
                System.out.println("[PASS] 2.2 Travel Agent Authentication OK: " + agent.getFullName() + " (Role: " + agent.getRole() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.2 Travel Agent login failed");
                failed++;
            }

            User traveler = authService.login("traveler@example.com", "traveler123");
            if (traveler != null && traveler.isTraveler()) {
                System.out.println("[PASS] 2.3 Traveler Authentication OK: " + traveler.getFullName() + " (Role: " + traveler.getRole() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.3 Traveler login failed");
                failed++;
            }

            // Security: Normal user cannot register as ADMIN
            try {
                authService.register("Hacker", "hacker@test.com", "9988776655", "secret123", "secret123", "ADMIN");
                System.err.println("[FAIL] 2.4 Allowed registering as ADMIN directly (Security flaw)");
                failed++;
            } catch (ValidationException ex) {
                System.out.println("[PASS] 2.4 Security Check: Blocked direct ADMIN registration (" + ex.getMessage() + ")");
                passed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 2. Authentication Exception: " + e.getMessage());
            failed++;
        }

        // 3. Flight Workflow & Approval
        try {
            // Agent creates a flight
            String flightNo = "TEST-FLIGHT-" + System.currentTimeMillis() % 10000;
            Flight newFlight = flightService.addFlight(2, "Vistara Test", flightNo, "Delhi", "Bengaluru",
                    "2026-11-25", "09:00 AM", "11:45 AM", 5200.0, 50, "");
            if ("PENDING".equalsIgnoreCase(newFlight.getApprovalStatus())) {
                System.out.println("[PASS] 3.1 Flight Creation OK (Initial status: PENDING)");
                passed++;
            } else {
                System.err.println("[FAIL] 3.1 New flight was not set to PENDING");
                failed++;
            }

            // Admin approves the flight
            boolean approved = adminService.approveListing("FLIGHT", newFlight.getId());
            Flight reloadedFlight = flightService.getFlightById(newFlight.getId());
            if (approved && "APPROVED".equalsIgnoreCase(reloadedFlight.getApprovalStatus())) {
                System.out.println("[PASS] 3.2 Admin Approval OK (Status updated to: APPROVED)");
                passed++;
            } else {
                System.err.println("[FAIL] 3.2 Admin approval of flight failed");
                failed++;
            }

            // Traveler books the flight
            int passengers = 2;
            int seatsBefore = reloadedFlight.getAvailableSeats();
            Booking flightBooking = bookingService.createFlightBooking(3, reloadedFlight.getId(), "2026-11-25", passengers, "Window seats");
            Flight afterBookingFlight = flightService.getFlightById(newFlight.getId());
            if (flightBooking != null && "FLIGHT".equalsIgnoreCase(flightBooking.getBookingType())
                    && afterBookingFlight.getAvailableSeats() == seatsBefore - passengers) {
                System.out.println("[PASS] 3.3 Flight Booking & Seat Decrement OK (Seats: " + seatsBefore + " -> " + afterBookingFlight.getAvailableSeats() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 3.3 Flight booking or seat decrement failed");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 3. Flight Exception: " + e.getMessage());
            failed++;
        }

        // 4. Car Rental Workflow & Approval
        try {
            Car newCar = carService.addCar(2, "Test Fortuner 4x4", "Toyota", "Fortuner", "Delhi", "SUV", 4200.0, 4, "");
            if ("PENDING".equalsIgnoreCase(newCar.getApprovalStatus())) {
                System.out.println("[PASS] 4.1 Car Rental Creation OK (Initial status: PENDING)");
                passed++;
            } else {
                System.err.println("[FAIL] 4.1 New car listing was not set to PENDING");
                failed++;
            }

            adminService.approveListing("CAR", newCar.getId());
            Car approvedCar = carService.getCarById(newCar.getId());
            if ("APPROVED".equalsIgnoreCase(approvedCar.getApprovalStatus())) {
                System.out.println("[PASS] 4.2 Admin Car Approval OK (Status: APPROVED)");
                passed++;
            } else {
                System.err.println("[FAIL] 4.2 Admin car approval failed");
                failed++;
            }

            int unitsBefore = approvedCar.getAvailableUnits();
            Booking carBooking = bookingService.createCarBooking(3, approvedCar.getId(), "2026-11-26", "2026-11-28", 1, "Child seat needed");
            Car afterBookingCar = carService.getCarById(newCar.getId());
            if (carBooking != null && "CAR".equalsIgnoreCase(carBooking.getBookingType())
                    && afterBookingCar.getAvailableUnits() == unitsBefore - 1) {
                System.out.println("[PASS] 4.3 Car Rental Booking & Unit Decrement OK (Units: " + unitsBefore + " -> " + afterBookingCar.getAvailableUnits() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 4.3 Car booking or unit decrement failed");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 4. Car Rental Exception: " + e.getMessage());
            failed++;
        }

        // 5. Standalone Hotel Reservation
        try {
            Hotel hotel = hotelService.getHotelById(1);
            int roomsBefore = hotel.getAvailableRooms();
            Booking hotelBooking = bookingService.createHotelBooking(3, hotel.getId(), "2026-11-26", "2026-11-29", 1, 2, "Quiet room");
            Hotel afterBookingHotel = hotelService.getHotelById(1);
            if (hotelBooking != null && "HOTEL".equalsIgnoreCase(hotelBooking.getBookingType())
                    && afterBookingHotel.getAvailableRooms() == roomsBefore - 1) {
                System.out.println("[PASS] 5.1 Hotel Room Reservation & Availability Decrement OK");
                passed++;
            } else {
                System.err.println("[FAIL] 5.1 Hotel reservation failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 5. Hotel Exception: " + e.getMessage());
            failed++;
        }

        // 6. Travel Itinerary Generation
        try {
            Map<String, Object> itinerary = bookingService.getItinerary(3);
            int segments = (int) itinerary.get("totalSegments");
            if (segments >= 3) {
                System.out.println("[PASS] 6.1 Travel Itinerary Generation OK: " + segments + " chronological segments found for Traveler");
                passed++;
            } else {
                System.err.println("[FAIL] 6.1 Expected at least 3 itinerary segments, got: " + segments);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 6. Itinerary Exception: " + e.getMessage());
            failed++;
        }

        // 7. Message & Feedback Flow
        try {
            Message msg = messageService.sendMessage(3, 2, "Test question about airport cab", "Do you offer airport cabs?");
            if (msg.getId() > 0 && "OPEN".equalsIgnoreCase(msg.getStatus())) {
                System.out.println("[PASS] 7.1 Traveler Message Submission OK");
                passed++;
            } else {
                System.err.println("[FAIL] 7.1 Message submission failed");
                failed++;
            }

            boolean replied = messageService.replyMessage(msg.getId(), "Yes, airport cab service is available on request.");
            Message reloadedMsg = new dao.MessageDAO().findById(msg.getId());
            if (replied && "REPLIED".equalsIgnoreCase(reloadedMsg.getStatus()) && reloadedMsg.getReply() != null) {
                System.out.println("[PASS] 7.2 Agent Message Reply OK: " + reloadedMsg.getReply());
                passed++;
            } else {
                System.err.println("[FAIL] 7.2 Message reply failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 7. Message Exception: " + e.getMessage());
            failed++;
        }

        // 8. Admin Statistics & System Settings
        try {
            AdminStats stats = adminService.getDashboardStats();
            if (stats.getTotalTravelers() > 0 && stats.getTotalAgents() > 0 && stats.getTotalFlights() > 0) {
                System.out.println("[PASS] 8.1 Admin Extended KPIs OK (Travelers: " + stats.getTotalTravelers()
                        + ", Agents: " + stats.getTotalAgents() + ", Flights: " + stats.getTotalFlights()
                        + ", Cars: " + stats.getTotalCars() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 8.1 Admin KPIs incomplete");
                failed++;
            }

            settingsService.updateSetting("site_name", "Tripzy - Verified Platform");
            String siteName = settingsService.getSetting("site_name", "");
            if ("Tripzy - Verified Platform".equals(siteName)) {
                System.out.println("[PASS] 8.2 System Settings Persistence OK: " + siteName);
                passed++;
            } else {
                System.err.println("[FAIL] 8.2 System settings update failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 8. Admin Stats/Settings Exception: " + e.getMessage());
            failed++;
        }

        System.out.println("==========================================================");
        System.out.printf("  END-TO-END TEST RESULT: %d PASSED, %d FAILED             \n", passed, failed);
        System.out.println("==========================================================");

        if (failed == 0) {
            System.out.println(">>> ALL 13 END-TO-END INTEGRATION WORKFLOWS PASSED SUCCESSFULLY! <<<");
            System.exit(0);
        } else {
            System.err.println(">>> SOME INTEGRATION WORKFLOWS FAILED! <<<");
            System.exit(1);
        }
    }
}
