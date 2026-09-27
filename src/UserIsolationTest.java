import model.*;
import service.*;
import util.DatabaseConnection;
import util.SessionManager;

import java.util.List;
import java.util.Map;

/**
 * UserIsolationTest
 * 
 * Specifically validates:
 * 1. Demo traveler (John Traveler) has existing seed bookings and itinerary.
 * 2. Newly registered traveler starts with strictly 0 bookings, 0 confirmed bookings,
 *    0 total investment, and 0 itinerary segments.
 * 3. Session token issuance and retrieval via SessionManager.
 * 4. When a new traveler makes 1 booking, only that 1 booking appears in their dashboard.
 * 5. Other users' (John Traveler) bookings remain completely untouched and separate.
 * 6. Cross-user authorization: User A cannot cancel User B's booking (ValidationException thrown).
 * 7. Multiple newly registered users do not leak or share bookings with each other.
 */
public class UserIsolationTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   USER DATA ISOLATION & DASHBOARD VERIFICATION TEST       ");
        System.out.println("==========================================================");

        int passed = 0;
        int failed = 0;

        AuthService authService = new AuthService();
        BookingService bookingService = new BookingService();
        FlightService flightService = new FlightService();
        MessageService messageService = new MessageService();

        // Step 1: Database Connectivity
        try {
            if (DatabaseConnection.testConnection()) {
                System.out.println("[PASS] 1. Database Connection OK");
                passed++;
            } else {
                System.err.println("[FAIL] 1. Database Connection Failed");
                failed++;
                return;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 1. Database Connection Exception: " + e.getMessage());
            failed++;
            return;
        }

        // Step 2: Verify Demo User (John Traveler) Data Exists and is Untouched
        User john = null;
        List<Booking> johnInitialBookings = null;
        try {
            john = authService.login("traveler@example.com", "traveler123");
            if (john != null && john.isTraveler()) {
                System.out.println("[PASS] 2.1 Demo Traveler (John) Authenticated: ID=" + john.getId() + ", Email=" + john.getEmail());
                passed++;
            } else {
                System.err.println("[FAIL] 2.1 Demo Traveler login failed");
                failed++;
                return;
            }

            johnInitialBookings = bookingService.getUserBookings(john.getId());
            if (johnInitialBookings != null && !johnInitialBookings.isEmpty()) {
                double johnTotalInvested = johnInitialBookings.stream()
                        .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                        .mapToDouble(Booking::getTotalAmount)
                        .sum();
                System.out.println("[PASS] 2.2 Demo Traveler (John) has " + johnInitialBookings.size() + 
                                   " seed bookings (Total active investment: ₹" + String.format("%.2f", johnTotalInvested) + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.2 Demo Traveler has no bookings (Seed data missing)");
                failed++;
            }

            Map<String, Object> johnItinerary = bookingService.getItinerary(john.getId());
            int johnSegments = (int) johnItinerary.getOrDefault("totalSegments", 0);
            if (johnSegments > 0) {
                System.out.println("[PASS] 2.3 Demo Traveler (John) has " + johnSegments + " itinerary segments");
                passed++;
            } else {
                System.err.println("[FAIL] 2.3 Demo Traveler itinerary segments is 0");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 2. Demo User Inspection Exception: " + e.getMessage());
            failed++;
        }

        // Step 3: Register a Brand New Traveler and Verify Pure Zero-State
        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 100000);
        String newEmail = "iso_traveler_" + uniqueSuffix + "@test.com";
        User newTraveler = null;
        try {
            newTraveler = authService.register(
                    "Isolated Traveler " + uniqueSuffix,
                    newEmail,
                    "98765" + String.format("%05d", Integer.parseInt(uniqueSuffix)),
                    "StrongPass123!",
                    "StrongPass123!",
                    "TRAVELER"
            );

            if (newTraveler != null && newTraveler.getId() > 0) {
                System.out.println("[PASS] 3.1 New Traveler Registered: ID=" + newTraveler.getId() + ", Email=" + newEmail);
                passed++;
            } else {
                System.err.println("[FAIL] 3.1 New Traveler registration failed");
                failed++;
                return;
            }

            // Verify newly registered user starts with ZERO bookings
            List<Booking> newBookings = bookingService.getUserBookings(newTraveler.getId());
            if (newBookings != null && newBookings.isEmpty()) {
                System.out.println("[PASS] 3.2 New Traveler has strictly 0 bookings (getUserBookings returned empty list)");
                passed++;
            } else {
                System.err.println("[FAIL] 3.2 New Traveler leaked existing bookings! Count=" + (newBookings == null ? "null" : newBookings.size()));
                failed++;
            }

            // Verify Itinerary is empty (0 segments)
            Map<String, Object> newItinerary = bookingService.getItinerary(newTraveler.getId());
            int newSegments = (int) newItinerary.getOrDefault("totalSegments", 0);
            if (newSegments == 0) {
                System.out.println("[PASS] 3.3 New Traveler has 0 upcoming itinerary segments");
                passed++;
            } else {
                System.err.println("[FAIL] 3.3 New Traveler has itinerary segments from another user! Count=" + newSegments);
                failed++;
            }

            // Verify Messages is empty (0 messages)
            List<Message> newMessages = messageService.getUserMessages(newTraveler.getId());
            if (newMessages != null && newMessages.isEmpty()) {
                System.out.println("[PASS] 3.4 New Traveler has 0 messages/support inquiries");
                passed++;
            } else {
                System.err.println("[FAIL] 3.4 New Traveler leaked support messages! Count=" + (newMessages == null ? "null" : newMessages.size()));
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 3. New Traveler Registration Exception: " + e.getMessage());
            failed++;
        }

        // Step 4: Verify SessionManager Token Lifecycle
        try {
            String token = SessionManager.createWebSession(newTraveler);
            if (token != null && !token.trim().isEmpty()) {
                System.out.println("[PASS] 4.1 Web session token successfully issued: " + token.substring(0, 10) + "...");
                passed++;
            } else {
                System.err.println("[FAIL] 4.1 Failed to create web session token");
                failed++;
            }

            User resolvedUser = SessionManager.getWebUser(token);
            if (resolvedUser != null && resolvedUser.getId() == newTraveler.getId()) {
                System.out.println("[PASS] 4.2 Web session correctly resolved user ID: " + resolvedUser.getId());
                passed++;
            } else {
                System.err.println("[FAIL] 4.2 Web session failed to resolve correct user");
                failed++;
            }

            User invalidUser = SessionManager.getWebUser("bogus-session-token-999");
            if (invalidUser == null) {
                System.out.println("[PASS] 4.3 Invalid session token correctly returns null");
                passed++;
            } else {
                System.err.println("[FAIL] 4.3 Invalid session token unexpectedly resolved a user!");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4. SessionManager Exception: " + e.getMessage());
            failed++;
        }

        // Step 5: Make EXACTLY 1 Booking as the New Traveler and Verify Dashboard Isolation
        Booking createdBooking = null;
        try {
            // Find an approved flight
            List<Flight> flights = flightService.getAllFlights();
            Flight targetFlight = flights.stream()
                    .filter(f -> "APPROVED".equalsIgnoreCase(f.getApprovalStatus()) && f.getAvailableSeats() >= 1)
                    .findFirst()
                    .orElse(null);

            if (targetFlight == null) {
                // If none approved, create one as agent and approve as admin
                System.out.println("[INFO] Creating test flight for booking...");
                Flight nf = flightService.addFlight(2, "Air Test", "AT-" + uniqueSuffix, "Delhi", "Mumbai",
                        "2026-12-15", "10:00 AM", "12:15 PM", 4500.0, 50, "");
                new AdminService().approveListing("FLIGHT", nf.getId());
                targetFlight = flightService.getFlightById(nf.getId());
            }

            createdBooking = bookingService.createFlightBooking(newTraveler.getId(), targetFlight.getId(), "2026-12-15", 1, "Window seat requested");
            if (createdBooking != null && createdBooking.getId() > 0) {
                System.out.println("[PASS] 5.1 Created single flight booking for New Traveler: BookingCode=" + createdBooking.getBookingCode());
                passed++;
            } else {
                System.err.println("[FAIL] 5.1 Failed to create booking for New Traveler");
                failed++;
                return;
            }

            // Re-fetch new traveler's bookings
            List<Booking> updatedNewBookings = bookingService.getUserBookings(newTraveler.getId());
            if (updatedNewBookings != null && updatedNewBookings.size() == 1) {
                Booking b = updatedNewBookings.get(0);
                if (b.getId() == createdBooking.getId() && b.getUserId() == newTraveler.getId()) {
                    System.out.println("[PASS] 5.2 New Traveler dashboard now shows EXACTLY 1 booking (Booking ID: " + b.getId() + ", Total: ₹" + b.getTotalAmount() + ")");
                    passed++;
                } else {
                    System.err.println("[FAIL] 5.2 Booking user ID mismatch: expected " + newTraveler.getId() + " but got " + b.getUserId());
                    failed++;
                }
            } else {
                System.err.println("[FAIL] 5.2 Expected exactly 1 booking for new traveler, but found: " + (updatedNewBookings == null ? "null" : updatedNewBookings.size()));
                failed++;
            }

            // Verify New Traveler's itinerary now has exactly 1 segment
            Map<String, Object> updatedNewItinerary = bookingService.getItinerary(newTraveler.getId());
            int updatedSegments = (int) updatedNewItinerary.getOrDefault("totalSegments", 0);
            if (updatedSegments == 1) {
                System.out.println("[PASS] 5.3 New Traveler itinerary now contains EXACTLY 1 segment");
                passed++;
            } else {
                System.err.println("[FAIL] 5.3 Expected 1 itinerary segment, but got: " + updatedSegments);
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 5. Booking Creation Exception: " + e.getMessage());
            failed++;
        }

        // Step 6: Verify John Traveler's Seed Bookings Remain Untouched
        try {
            List<Booking> johnAfterBookings = bookingService.getUserBookings(john.getId());
            if (johnAfterBookings != null && johnAfterBookings.size() == johnInitialBookings.size()) {
                boolean containsNewBooking = false;
                if (createdBooking != null) {
                    final int newBookingId = createdBooking.getId();
                    containsNewBooking = johnAfterBookings.stream().anyMatch(b -> b.getId() == newBookingId);
                }

                if (!containsNewBooking) {
                    System.out.println("[PASS] 6.1 Demo Traveler (John) bookings count unchanged (" + johnAfterBookings.size() + ") and does NOT contain new traveler's booking");
                    passed++;
                } else {
                    System.err.println("[FAIL] 6.1 Data contamination! John's bookings list contains new traveler's booking ID " + createdBooking.getId());
                    failed++;
                }
            } else {
                System.err.println("[FAIL] 6.1 John's bookings count changed unexpectedly!");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 6. Cross-User Data Leakage Inspection Exception: " + e.getMessage());
            failed++;
        }

        // Step 7: Cross-User Authorization & Security Checks
        try {
            // New traveler attempts to cancel John's booking
            if (johnInitialBookings != null && !johnInitialBookings.isEmpty() && createdBooking != null) {
                int johnBookingId = johnInitialBookings.get(0).getId();
                try {
                    bookingService.cancelBooking(johnBookingId, newTraveler.getId(), false);
                    System.err.println("[FAIL] 7.1 Security flaw! New Traveler was allowed to cancel John's booking!");
                    failed++;
                } catch (ValidationException ve) {
                    System.out.println("[PASS] 7.1 Access Control OK: Blocked New Traveler from cancelling John's booking (" + ve.getMessage() + ")");
                    passed++;
                }

                // John attempts to cancel New Traveler's booking
                try {
                    bookingService.cancelBooking(createdBooking.getId(), john.getId(), false);
                    System.err.println("[FAIL] 7.2 Security flaw! John Traveler was allowed to cancel New Traveler's booking!");
                    failed++;
                } catch (ValidationException ve) {
                    System.out.println("[PASS] 7.2 Access Control OK: Blocked John from cancelling New Traveler's booking (" + ve.getMessage() + ")");
                    passed++;
                }

                // New traveler cancels their OWN booking
                boolean selfCancelled = bookingService.cancelBooking(createdBooking.getId(), newTraveler.getId(), false);
                if (selfCancelled) {
                    System.out.println("[PASS] 7.3 New Traveler successfully cancelled their own booking");
                    passed++;
                } else {
                    System.err.println("[FAIL] 7.3 New Traveler failed to cancel their own booking");
                    failed++;
                }
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 7. Authorization Security Exception: " + e.getMessage());
            failed++;
        }

        // Step 8: Multi-User Isolation: Register a Second New Traveler
        try {
            String suffix2 = String.valueOf((System.currentTimeMillis() + 999) % 100000);
            String email2 = "iso_traveler2_" + suffix2 + "@test.com";
            User traveler2 = authService.register(
                    "Second Traveler " + suffix2,
                    email2,
                    "98765" + String.format("%05d", Integer.parseInt(suffix2)),
                    "PassSecond123!",
                    "PassSecond123!",
                    "TRAVELER"
            );

            List<Booking> bookings2 = bookingService.getUserBookings(traveler2.getId());
            if (bookings2 != null && bookings2.isEmpty()) {
                System.out.println("[PASS] 8.1 Second Traveler starts with 0 bookings (No leakage from traveler 1 or demo traveler)");
                passed++;
            } else {
                System.err.println("[FAIL] 8.1 Second Traveler has non-zero bookings: " + (bookings2 == null ? "null" : bookings2.size()));
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 8. Second Traveler Exception: " + e.getMessage());
            failed++;
        }

        System.out.println("==========================================================");
        System.out.println("  SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
