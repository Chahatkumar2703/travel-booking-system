import model.*;
import service.*;
import util.DatabaseConnection;
import util.SessionManager;

import java.util.List;

/**
 * Programmatic smoke and integration test verifying all core system workflows.
 */
public class TestSystem {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   ONLINE TRAVEL BOOKING SYSTEM - TEST SUITE     ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        // 1. Test Database Connection
        try {
            boolean connected = DatabaseConnection.testConnection();
            if (connected) {
                System.out.println("[PASS] 1. Database Connection OK (MySQL 8.0 connected)");
                passed++;
            } else {
                System.err.println("[FAIL] 1. Database Connection Failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 1. Database Connection Exception: " + e.getMessage());
            failed++;
        }

        // 2. Test Authentication (Admin & User)
        AuthService authService = new AuthService();
        try {
            User admin = authService.login("admin@travel.com", "admin123");
            if (admin != null && admin.isAdmin()) {
                System.out.println("[PASS] 2.1 Admin Authentication OK: " + admin.getFullName() + " (Role: " + admin.getRole() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.1 Admin Login failed or role mismatch");
                failed++;
            }

            User user = authService.login("priya@example.com", "user123");
            if (user != null && !user.isAdmin()) {
                System.out.println("[PASS] 2.2 Customer Authentication OK: " + user.getFullName() + " (Role: " + user.getRole() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.2 Customer Login failed");
                failed++;
            }

            // Test registration
            String testEmail = "testuser" + System.currentTimeMillis() + "@traveltest.com";
            User registered = authService.register("Test Traveler", testEmail, "9988776655", "secret123", "secret123");
            if (registered != null && registered.getId() > 0) {
                System.out.println("[PASS] 2.3 Registration OK: " + registered.getEmail() + " (ID: " + registered.getId() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2.3 Registration failed");
                failed++;
            }

            // Test wrong password check
            try {
                authService.login("admin@travel.com", "wrongpass");
                System.err.println("[FAIL] 2.4 Wrong password check failed (allowed login)");
                failed++;
            } catch (AuthenticationException ex) {
                System.out.println("[PASS] 2.4 Wrong Password Guard OK: " + ex.getMessage());
                passed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 2. Authentication Exception: " + e.getMessage());
            failed++;
        }

        // 3. Test Destinations
        DestinationService destService = new DestinationService();
        try {
            List<Destination> destinations = destService.getAllDestinations();
            if (desticeValidCount(destinations, 8)) {
                System.out.println("[PASS] 3.1 Destinations Retrieval OK: " + destinations.size() + " destinations loaded");
                passed++;
            } else {
                System.err.println("[FAIL] 3.1 Expected at least 8 destinations, got: " + (destinations != null ? destinations.size() : 0));
                failed++;
            }

            List<Destination> searchResults = destService.searchDestinations("Manali");
            if (!searchResults.isEmpty() && "Manali".equalsIgnoreCase(searchResults.get(0).getName())) {
                System.out.println("[PASS] 3.2 Destination Search OK: Found " + searchResults.get(0).getName());
                passed++;
            } else {
                System.err.println("[FAIL] 3.2 Destination search for Manali failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 3. Destinations Exception: " + e.getMessage());
            failed++;
        }

        // 4. Test Packages
        PackageService pkgService = new PackageService();
        try {
            List<TravelPackage> packages = pkgService.getAllPackages();
            if (desticeValidCount(packages, 10)) {
                System.out.println("[PASS] 4.1 Packages Retrieval OK: " + packages.size() + " packages loaded");
                passed++;
            } else {
                System.err.println("[FAIL] 4.1 Expected at least 10 packages, got: " + (packages != null ? packages.size() : 0));
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4. Packages Exception: " + e.getMessage());
            failed++;
        }

        // 5. Test Hotels
        HotelService hotelService = new HotelService();
        try {
            List<Hotel> hotels = hotelService.getAllHotels();
            if (desticeValidCount(hotels, 8)) {
                System.out.println("[PASS] 5.1 Hotels Retrieval OK: " + hotels.size() + " hotels loaded");
                passed++;
            } else {
                System.err.println("[FAIL] 5.1 Expected at least 8 hotels, got: " + (hotels != null ? hotels.size() : 0));
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 5. Hotels Exception: " + e.getMessage());
            failed++;
        }

        // 6. Test Booking Workflow & Calculation
        BookingService bookingService = new BookingService();
        PaymentService paymentService = new PaymentService();
        try {
            TravelPackage pkg = pkgService.getPackageById(2); // Manali Adventure (₹12,999, 4 nights)
            Hotel hotel = hotelService.getHotelById(3); // Himalayan Spa Resort (₹3,800/night)
            int initialRooms = hotel.getAvailableRooms();

            int persons = 2;
            double[] costs = bookingService.calculateCost(pkg, hotel, persons);
            double expectedPackageCost = 12999.00 * 2; // 25,998
            double expectedHotelCost = 3800.00 * 4;   // 15,200
            double expectedTotal = expectedPackageCost + expectedHotelCost; // 41,198

            if (Math.abs(costs[0] - expectedPackageCost) < 0.01 &&
                Math.abs(costs[1] - expectedHotelCost) < 0.01 &&
                Math.abs(costs[2] - expectedTotal) < 0.01) {
                System.out.println("[PASS] 6.1 Cost Calculation OK: Package=₹" + costs[0] + ", Hotel=₹" + costs[1] + ", Total=₹" + costs[2]);
                passed++;
            } else {
                System.err.println("[FAIL] 6.1 Cost calculation mismatch! Got: " + costs[2] + ", expected: " + expectedTotal);
                failed++;
            }

            // Create booking
            Booking b = bookingService.createBooking(2, pkg.getId(), hotel.getId(), "2026-11-20", persons, "Test booking special requests");
            if (b != null && b.getBookingCode().startsWith("TB-2026-")) {
                System.out.println("[PASS] 6.2 Booking Creation OK: " + b.getBookingCode() + " (Total: ₹" + b.getTotalAmount() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 6.2 Booking creation failed");
                failed++;
            }

            // Verify hotel room was decremented
            Hotel updatedHotel = hotelService.getHotelById(hotel.getId());
            if (updatedHotel.getAvailableRooms() == initialRooms - 1) {
                System.out.println("[PASS] 6.3 Room Allocation OK: Rooms decremented from " + initialRooms + " to " + updatedHotel.getAvailableRooms());
                passed++;
            } else {
                System.err.println("[FAIL] 6.3 Room count not decremented properly");
                failed++;
            }

            // 7. Test Simulated Payment
            PaymentResult pResult = paymentService.executePayment(b.getId(), 2, b.getTotalAmount(), "UPI", "priya@okhdfcbank");
            if (pResult != null && pResult.isSuccess() && pResult.getTransactionCode().startsWith("TXN-UPI-")) {
                System.out.println("[PASS] 7.1 Simulated Payment OK: " + pResult.getTransactionCode() + " (Status: " + pResult.getStatus() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 7.1 Simulated payment failed");
                failed++;
            }

            // 8. Test Cancellation & Room Restoration
            boolean cancelled = bookingService.cancelBooking(b.getId(), 2, false);
            Booking reloaded = bookingService.getBookingById(b.getId());
            Hotel restoredHotel = hotelService.getHotelById(hotel.getId());

            if (cancelled && "CANCELLED".equalsIgnoreCase(reloaded.getBookingStatus()) && restoredHotel.getAvailableRooms() == initialRooms) {
                System.out.println("[PASS] 8.1 Cancellation & Room Restoration OK: Status is CANCELLED, rooms restored to " + restoredHotel.getAvailableRooms());
                passed++;
            } else {
                System.err.println("[FAIL] 8.1 Cancellation failed or room count not restored");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 6/7/8. Booking Exception: " + e.getMessage());
            failed++;
        }

        // 9. Test Admin Statistics
        AdminService adminService = new AdminService();
        try {
            AdminStats stats = adminService.getDashboardStats();
            if (stats.getTotalUsers() > 0 && stats.getTotalDestinations() >= 8 && stats.getTotalRevenue() > 0) {
                System.out.println("[PASS] 9.1 Admin KPIs OK: Users=" + stats.getTotalUsers() +
                        ", Destinations=" + stats.getTotalDestinations() +
                        ", Packages=" + stats.getTotalPackages() +
                        ", Hotels=" + stats.getTotalHotels() +
                        ", Bookings=" + stats.getTotalBookings() +
                        ", Revenue=₹" + stats.getTotalRevenue());
                passed++;
            } else {
                System.err.println("[FAIL] 9.1 Admin stats invalid values");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 9. Admin Stats Exception: " + e.getMessage());
            failed++;
        }

        System.out.println("=================================================");
        System.out.printf("   TEST SUMMARY: %d PASSED, %d FAILED             \n", passed, failed);
        System.out.println("=================================================");

        if (failed == 0) {
            System.out.println("ALL SYSTEM VERIFICATIONS PASSED SUCCESSFULLY!");
            System.exit(0);
        } else {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static boolean desticeValidCount(List<?> list, int minCount) {
        return list != null && list.size() >= minCount;
    }
}
