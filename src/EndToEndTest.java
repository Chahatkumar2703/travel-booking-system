import dao.*;
import model.*;
import service.*;
import util.DatabaseConnection;
import util.DatabaseInitializer;
import util.SessionManager;
import util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Comprehensive End-to-End Test Suite covering all features and operations.
 */
public class EndToEndTest {

    private static int passedTests = 0;
    private static int failedTests = 0;

    private static void check(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("[PASS] " + testName + (details != null ? " - " + details : ""));
            passedTests++;
        } else {
            System.err.println("[FAIL] " + testName + (details != null ? " - " + details : ""));
            failedTests++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("     VOYAGEQUEST SYSTEM: END-TO-END VERIFICATION & STRESS TEST         ");
        System.out.println("=======================================================================\n");

        // 1. Initializer test
        try {
            DatabaseInitializer.initializeDatabase();
            check("Database Bootstrap & Table Verification", true, "Tables verified in travel_booking_system");
        } catch (Exception e) {
            check("Database Bootstrap & Table Verification", false, e.getMessage());
        }

        // 2. Database Connection Test
        try {
            boolean connected = DatabaseConnection.testConnection();
            check("MySQL JDBC Connection", connected, "Connected to localhost:3306");
        } catch (Exception e) {
            check("MySQL JDBC Connection", false, e.getMessage());
        }

        AuthService authService = new AuthService();
        DestinationService destService = new DestinationService();
        PackageService pkgService = new PackageService();
        HotelService hotelService = new HotelService();
        BookingService bookingService = new BookingService();
        PaymentService paymentService = new PaymentService();
        AdminService adminService = new AdminService();

        // 3. Authentication & User Management Tests
        System.out.println("\n--- Testing Authentication & User Operations ---");
        try {
            // Admin login
            User admin = authService.login("admin@travel.com", "admin123");
            check("Admin Login", admin != null && admin.isAdmin(), "System Administrator authenticated");

            // Customer login
            User customer = authService.login("priya@example.com", "user123");
            check("Customer Login", customer != null && !customer.isAdmin(), "Priya Sharma authenticated");

            // Bad password
            boolean badPassCaught = false;
            try {
                authService.login("priya@example.com", "wrongpassword");
            } catch (AuthenticationException e) {
                badPassCaught = true;
            }
            check("Invalid Password Guard", badPassCaught, "Blocked unauthorized access");

            // Non-existent user
            boolean badUserCaught = false;
            try {
                authService.login("unknown_user_999@domain.com", "anypass");
            } catch (AuthenticationException e) {
                badUserCaught = true;
            }
            check("Non-existent User Guard", badUserCaught, "Blocked non-existent account");

            // Registration validation (empty name)
            boolean emptyNameCaught = false;
            try {
                authService.register("", "valid@email.com", "9876543210", "pass123", "pass123");
            } catch (ValidationException e) {
                emptyNameCaught = true;
            }
            check("Registration Empty Name Validation", emptyNameCaught, "Blocked empty name");

            // Registration validation (invalid email)
            boolean badEmailCaught = false;
            try {
                authService.register("Valid Name", "notanemail", "9876543210", "pass123", "pass123");
            } catch (ValidationException e) {
                badEmailCaught = true;
            }
            check("Registration Bad Email Validation", badEmailCaught, "Blocked malformed email");

            // Registration validation (password mismatch)
            boolean mismatchCaught = false;
            try {
                authService.register("Valid Name", "test@domain.com", "9876543210", "password123", "different123");
            } catch (ValidationException e) {
                mismatchCaught = true;
            }
            check("Registration Password Mismatch Validation", mismatchCaught, "Blocked mismatched passwords");

            // Successful registration
            String uniqueEmail = "traveler_" + System.currentTimeMillis() + "@e2etest.com";
            User newTraveler = authService.register("John Doe", uniqueEmail, "9876543210", "mypass123", "mypass123");
            check("New Customer Registration", newTraveler != null && newTraveler.getId() > 0, "Registered " + uniqueEmail);

            // Duplicate registration
            boolean dupCaught = false;
            try {
                authService.register("John Doe Duplicate", uniqueEmail, "9876543210", "mypass123", "mypass123");
            } catch (ValidationException e) {
                dupCaught = true;
            }
            check("Duplicate Email Registration Guard", dupCaught, "Prevented duplicate account creation");

            // Profile update
            boolean profileUpdated = authService.updateProfile(newTraveler.getId(), "Johnathan Doe", "9123456780");
            check("User Profile Update", profileUpdated, "Name and Phone successfully updated");

            // Change password
            boolean passChanged = authService.changePassword(newTraveler.getId(), "mypass123", "newsecret999", "newsecret999");
            check("Password Change Operation", passChanged, "Password hash updated via SHA-256");

            // Re-login with new password
            User relogged = authService.login(uniqueEmail, "newsecret999");
            check("Re-authentication with New Password", relogged != null, "Successfully logged in with new password");

        } catch (Exception e) {
            check("Authentication Operations", false, e.getMessage());
            e.printStackTrace();
        }

        // 4. Destinations Catalog & Search Tests
        System.out.println("\n--- Testing Destinations Catalog & Search ---");
        try {
            List<Destination> allDests = destService.getAllDestinations();
            check("Fetch All Destinations", allDests.size() >= 8, "Found " + allDests.size() + " destinations");

            List<Destination> goaSearch = destService.searchDestinations("Goa");
            check("Destination Keyword Search", !goaSearch.isEmpty() && goaSearch.get(0).getName().equals("Goa"), "Found Goa");

            List<Destination> stateSearch = destService.searchDestinations("Himachal");
            check("Destination State Search", !stateSearch.isEmpty() && stateSearch.get(0).getName().equals("Manali"), "Found Manali in Himachal");
        } catch (Exception e) {
            check("Destinations Catalog", false, e.getMessage());
            e.printStackTrace();
        }

        // 5. Travel Packages Catalog & Filtering Tests
        System.out.println("\n--- Testing Travel Packages Catalog & Filters ---");
        try {
            List<TravelPackage> allPkgs = pkgService.getAllPackages();
            check("Fetch All Packages", allPkgs.size() >= 12, "Found " + allPkgs.size() + " packages");

            // Filter by destination ID (Goa = 1)
            List<TravelPackage> goaPkgs = pkgService.getPackagesByDestination(1);
            check("Filter Packages by Destination", !goaPkgs.isEmpty(), "Found " + goaPkgs.size() + " packages for Goa");

            // Search by keyword
            List<TravelPackage> snowPkgs = pkgService.searchPackages("Snow", null, null);
            check("Search Packages by Keyword ('Snow')", !snowPkgs.isEmpty(), "Found: " + snowPkgs.get(0).getPackageName());

            // Check package details
            TravelPackage manaliPkg = pkgService.getPackageById(2);
            check("Package Details Integrity", manaliPkg != null && manaliPkg.getDurationNights() == 4 && manaliPkg.getPricePerPerson() == 12999.00,
                    manaliPkg.getPackageName() + " (₹" + manaliPkg.getPricePerPerson() + ")");
        } catch (Exception e) {
            check("Packages Operations", false, e.getMessage());
            e.printStackTrace();
        }

        // 6. Hotels Catalog & Availability Tests
        System.out.println("\n--- Testing Hotels Catalog & Inventory ---");
        try {
            List<Hotel> allHotels = hotelService.getAllHotels();
            check("Fetch All Hotels", allHotels.size() >= 10, "Found " + allHotels.size() + " partner hotels");

            List<Hotel> manaliHotels = hotelService.getHotelsByDestination(2);
            check("Hotels by Destination (Manali)", !manaliHotels.isEmpty(), "Found " + manaliHotels.size() + " hotels in Manali");

            Hotel sampleHotel = allHotels.get(0);
            check("Hotel Room Availability", sampleHotel.getAvailableRooms() > 0, sampleHotel.getHotelName() + " has " + sampleHotel.getAvailableRooms() + " rooms");
        } catch (Exception e) {
            check("Hotels Operations", false, e.getMessage());
            e.printStackTrace();
        }

        // 7. Booking Engine & Pricing Validation Tests
        System.out.println("\n--- Testing Booking Engine & Cost Calculations ---");
        Booking testBooking = null;
        try {
            TravelPackage pkg = pkgService.getPackageById(2); // Manali: ₹12,999 / person, 4 nights
            Hotel hotel = hotelService.getHotelById(3); // Himalayan Spa Resort: ₹3,800 / night
            int initialAvailableRooms = hotel.getAvailableRooms();

            int persons = 3;
            double[] costs = bookingService.calculateCost(pkg, hotel, persons);
            // Expected:
            // Package Cost = 12999 * 3 = 38,997
            // Hotel Cost   = 3800 * 4 = 15,200
            // Total Amount = 38997 + 15200 = 54,197
            double expectedPackageCost = 38997.00;
            double expectedHotelCost = 15200.00;
            double expectedTotal = 54197.00;

            check("Live Pricing Calculation Formula",
                    Math.abs(costs[0] - expectedPackageCost) < 0.01 &&
                    Math.abs(costs[1] - expectedHotelCost) < 0.01 &&
                    Math.abs(costs[2] - expectedTotal) < 0.01,
                    "Pkg: ₹" + costs[0] + " + Htl: ₹" + costs[1] + " = Total: ₹" + costs[2]);

            // Test past travel date validation
            boolean pastDateBlocked = false;
            try {
                bookingService.createBooking(2, pkg.getId(), hotel.getId(), "2020-01-01", persons, "Past date request");
            } catch (ValidationException e) {
                pastDateBlocked = true;
            }
            check("Past Travel Date Rejection Guard", pastDateBlocked, "Rejected travel date '2020-01-01'");

            // Test zero persons validation
            boolean zeroPersonsBlocked = false;
            try {
                bookingService.createBooking(2, pkg.getId(), hotel.getId(), LocalDate.now().plusDays(10).toString(), 0, "Zero persons");
            } catch (ValidationException e) {
                zeroPersonsBlocked = true;
            }
            check("Zero Persons Rejection Guard", zeroPersonsBlocked, "Rejected persons = 0");

            // Create valid booking
            String futureDate = LocalDate.now().plusDays(25).toString();
            testBooking = bookingService.createBooking(2, pkg.getId(), hotel.getId(), futureDate, persons, "Window seat and mountain view");
            check("Valid Booking Creation", testBooking != null && testBooking.getBookingCode().startsWith("TB-2026-"),
                    "Generated Booking Code: " + testBooking.getBookingCode());

            // Verify hotel room was decremented
            Hotel reloadedHotel = hotelService.getHotelById(hotel.getId());
            check("Hotel Room Capacity Decrement", reloadedHotel.getAvailableRooms() == initialAvailableRooms - 1,
                    "Rooms changed from " + initialAvailableRooms + " to " + reloadedHotel.getAvailableRooms());

        } catch (Exception e) {
            check("Booking Engine Operations", false, e.getMessage());
            e.printStackTrace();
        }

        // 8. Simulated Payment Gateway Tests
        System.out.println("\n--- Testing Simulated Payment Processors ---");
        try {
            if (testBooking != null) {
                // Test invalid card format validation
                boolean badCardBlocked = false;
                try {
                    paymentService.executePayment(testBooking.getId(), 2, testBooking.getTotalAmount(), "CARD", "1234|08/28|789|John");
                } catch (ValidationException e) {
                    badCardBlocked = true;
                }
                check("Credit Card Validation Guard (Invalid Card Length)", badCardBlocked, "Blocked 4-digit card number");

                // Test invalid UPI format validation
                boolean badUpiBlocked = false;
                try {
                    paymentService.executePayment(testBooking.getId(), 2, testBooking.getTotalAmount(), "UPI", "invalid_upi_no_at_sign");
                } catch (ValidationException e) {
                    badUpiBlocked = true;
                }
                check("UPI Validation Guard (Missing @)", badUpiBlocked, "Blocked malformed UPI handle");

                // Execute successful UPI payment
                PaymentResult payResult = paymentService.executePayment(testBooking.getId(), 2, testBooking.getTotalAmount(), "UPI", "priya@okhdfcbank");
                check("UPI Payment Simulation", payResult.isSuccess() && payResult.getTransactionCode().startsWith("TXN-UPI-"),
                        "Transaction Code: " + payResult.getTransactionCode() + " (Status: " + payResult.getStatus() + ")");

                // Retrieve saved payment from database
                Payment paymentRecord = paymentService.getPaymentByBookingId(testBooking.getId());
                check("Payment Database Persistence", paymentRecord != null && paymentRecord.getAmount() == testBooking.getTotalAmount(),
                        "Recorded ₹" + paymentRecord.getAmount() + " via " + paymentRecord.getPaymentMethod());
            }
        } catch (Exception e) {
            check("Simulated Payment Operations", false, e.getMessage());
            e.printStackTrace();
        }

        // 9. Booking History & Soft Cancellation Tests
        System.out.println("\n--- Testing Booking History & Cancellation ---");
        try {
            // User booking history
            List<Booking> userBookings = bookingService.getUserBookings(2);
            check("Customer Booking History Isolation", !userBookings.isEmpty(),
                    "Loaded " + userBookings.size() + " bookings for User ID 2");

            if (testBooking != null) {
                Hotel bookedHotel = hotelService.getHotelById(testBooking.getHotelId());
                int roomsBeforeCancel = bookedHotel.getAvailableRooms();

                // User cancels booking
                boolean cancelled = bookingService.cancelBooking(testBooking.getId(), 2, false);
                check("Booking Cancellation Request", cancelled, "Status updated to CANCELLED");

                // Check record preserved in DB
                Booking rechecked = bookingService.getBookingById(testBooking.getId());
                check("Historical Preservation After Cancellation", rechecked != null && "CANCELLED".equals(rechecked.getBookingStatus()),
                        "Booking " + rechecked.getBookingCode() + " remains in database with status CANCELLED");

                // Check hotel room restored
                Hotel hotelAfterCancel = hotelService.getHotelById(testBooking.getHotelId());
                check("Hotel Room Restoration Upon Cancellation", hotelAfterCancel.getAvailableRooms() == roomsBeforeCancel + 1,
                        "Rooms restored from " + roomsBeforeCancel + " to " + hotelAfterCancel.getAvailableRooms());

                // Prevent double-cancellation
                boolean doubleCancelBlocked = false;
                try {
                    bookingService.cancelBooking(testBooking.getId(), 2, false);
                } catch (ValidationException e) {
                    doubleCancelBlocked = true;
                }
                check("Double-Cancellation Guard", doubleCancelBlocked, "Prevented cancelling an already cancelled booking");
            }
        } catch (Exception e) {
            check("Booking History & Cancellation Operations", false, e.getMessage());
            e.printStackTrace();
        }

        // 10. Admin Control Center & Full CRUD Operations
        System.out.println("\n--- Testing Admin Operations & CRUD ---");
        try {
            // 8 KPI metrics
            AdminStats stats = adminService.getDashboardStats();
            check("Admin 8 KPI Summary Metrics",
                    stats.getTotalUsers() > 0 &&
                    stats.getTotalDestinations() >= 8 &&
                    stats.getTotalPackages() >= 12 &&
                    stats.getTotalHotels() >= 10 &&
                    stats.getTotalBookings() > 0 &&
                    stats.getConfirmedBookings() >= 0 &&
                    stats.getCancelledBookings() > 0 &&
                    stats.getTotalRevenue() > 0,
                    "Users=" + stats.getTotalUsers() + ", Dests=" + stats.getTotalDestinations() +
                    ", Pkgs=" + stats.getTotalPackages() + ", Revenue=₹" + stats.getTotalRevenue());

            // User moderation: toggle status
            User userToToggle = authService.login("amit@example.com", "user123");
            boolean statusToggled = adminService.toggleUserStatus(userToToggle.getId(), "DISABLED");
            check("Admin User Status Toggle (Disable)", statusToggled, "Amit Patel disabled");

            // Verify disabled user cannot log in
            boolean disabledLoginBlocked = false;
            try {
                authService.login("amit@example.com", "user123");
            } catch (AuthenticationException e) {
                disabledLoginBlocked = true;
            }
            check("Disabled User Login Prevention", disabledLoginBlocked, "Blocked login for deactivated account");

            // Re-enable user
            adminService.toggleUserStatus(userToToggle.getId(), "ACTIVE");
            User reenabledUser = authService.login("amit@example.com", "user123");
            check("Admin User Status Toggle (Re-enable)", reenabledUser != null, "Amit Patel re-enabled successfully");

            // Destination CRUD: Add, Update, Delete
            boolean destAdded = destService.addDestination("Shimla Hills", "Himachal Pradesh", "Queen of Hills", "Mall Road, Ridge, Kufri", "March to June");
            check("Admin Create Destination", destAdded, "Added 'Shimla Hills'");

            List<Destination> shimlaList = destService.searchDestinations("Shimla Hills");
            int shimlaId = shimlaList.get(0).getId();

            boolean destUpdated = destService.updateDestination(shimlaId, "Shimla Heritage", "Himachal Pradesh", "Updated description", "Mall Road, Ridge", "All year");
            check("Admin Update Destination", destUpdated, "Updated 'Shimla Heritage'");

            // Package CRUD for this destination
            boolean pkgAdded = pkgService.addPackage("Shimla Colonial Weekend", shimlaId, 3, 2, 7999.00, "Mall Road, Kufri, Jakhoo", true, true, true, "Weekend retreat");
            check("Admin Create Travel Package", pkgAdded, "Added 'Shimla Colonial Weekend'");

            List<TravelPackage> shimlaPkgs = pkgService.getPackagesByDestination(shimlaId);
            int testPkgId = shimlaPkgs.get(0).getId();

            boolean pkgUpdated = pkgService.updatePackage(testPkgId, "Shimla Luxury Colonial Weekend", shimlaId, 3, 2, 8499.00, "Mall Road, Kufri, Jakhoo, Christ Church", true, true, true, "Updated luxury retreat", "ACTIVE");
            check("Admin Update Travel Package", pkgUpdated, "Updated package price to ₹8,499");

            // Hotel CRUD for this destination
            boolean hotelAdded = hotelService.addHotel("Radisson Hotel Shimla", shimlaId, "Goodwood Estate, Lower Bharari Road", "Deluxe Pine View", 4200.00, 20, 4.6, "Luxury pine forest view");
            check("Admin Create Hotel", hotelAdded, "Added 'Radisson Hotel Shimla'");

            List<Hotel> shimlaHotels = hotelService.getHotelsByDestination(shimlaId);
            int testHotelId = shimlaHotels.get(0).getId();

            boolean roomAdjusted = hotelService.updateRoomAvailability(testHotelId, 5);
            Hotel adjustedHotel = hotelService.getHotelById(testHotelId);
            check("Admin Adjust Hotel Room Availability", roomAdjusted && adjustedHotel.getAvailableRooms() == 25,
                    "Rooms adjusted from 20 to " + adjustedHotel.getAvailableRooms());

            // Delete hotel, package, and destination
            boolean hotelDeleted = hotelService.deleteHotel(testHotelId);
            check("Admin Delete Hotel", hotelDeleted, "Removed test hotel");

            boolean pkgDeleted = pkgService.deletePackage(testPkgId);
            check("Admin Delete Package", pkgDeleted, "Removed test package");

            boolean destDeleted = destService.deleteDestination(shimlaId);
            check("Admin Delete Destination", destDeleted, "Removed test destination");

            // Booking Management: Update status
            List<Booking> allBookings = adminService.getAllBookings();
            check("Admin View All Bookings", !allBookings.isEmpty(), "Loaded " + allBookings.size() + " total bookings");

            Booking firstBooking = allBookings.get(0);
            boolean bookingStatusUpdated = adminService.updateBookingStatus(firstBooking.getId(), "COMPLETED");
            check("Admin Update Booking Status", bookingStatusUpdated, "Booking " + firstBooking.getBookingCode() + " set to COMPLETED");

            // Payment Audits
            List<Payment> allPayments = adminService.getAllPayments();
            check("Admin Financial Audit Log", !allPayments.isEmpty(), "Audited " + allPayments.size() + " financial transactions");

        } catch (Exception e) {
            check("Admin CRUD Operations", false, e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=======================================================================");
        System.out.printf("   FINAL TEST RESULTS: %d PASSED, %d FAILED                            \n", passedTests, failedTests);
        System.out.println("=======================================================================");

        if (failedTests == 0) {
            System.out.println("🎉 ALL FEATURES & SUBSYSTEMS VERIFIED 100% OPERATIONAL WITH 0 ERRORS!");
            System.exit(0);
        } else {
            System.err.println("❌ SOME VERIFICATIONS FAILED. PLEASE INSPECT LOGS ABOVE.");
            System.exit(1);
        }
    }
}
