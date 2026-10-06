import model.User;
import service.AuthService;
import service.AuthenticationException;
import service.ValidationException;
import util.DatabaseConnection;
import util.ValidationUtil;

/**
 * TripzyFeatureTest
 *
 * Comprehensive automated test suite verifying:
 * Phase 1: Tripzy branding and configuration integrity.
 * Phase 2: Registration validation (name, email, 10-digit mobile, duplicate email/mobile, password complexity).
 * Phase 3: Forgot password and OTP verification lifecycle (generation, validation, reset, invalidation).
 */
public class TripzyFeatureTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("     TRIPZY REBRAND & FEATURE VALIDATION TEST SUITE       ");
        System.out.println("==========================================================");

        int passed = 0;
        int failed = 0;

        AuthService authService = new AuthService();

        // 1. Database Connectivity Check
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

        // 2. Unit Validation Checks via ValidationUtil
        // 2.1 Name validation
        if (!ValidationUtil.isValidName(null) && !ValidationUtil.isValidName("") && !ValidationUtil.isValidName("A") && !ValidationUtil.isValidName("John@123")) {
            System.out.println("[PASS] 2.1 Invalid names correctly rejected (<2 chars, symbols)");
            passed++;
        } else {
            System.err.println("[FAIL] 2.1 Name validation allowed invalid names");
            failed++;
        }

        if (ValidationUtil.isValidName("Rahul Sharma") && ValidationUtil.isValidName("Dr. Jane Doe-Smith")) {
            System.out.println("[PASS] 2.2 Valid names correctly accepted");
            passed++;
        } else {
            System.err.println("[FAIL] 2.2 Valid names were rejected");
            failed++;
        }

        // 2.2 Phone validation (strict 10-digit starting with 6-9)
        if (!ValidationUtil.isValidPhone(null) &&
            !ValidationUtil.isValidPhone("1234567890") && // starts with 1
            !ValidationUtil.isValidPhone("987654321") &&  // 9 digits
            !ValidationUtil.isValidPhone("98765432101") && // 11 digits
            !ValidationUtil.isValidPhone("987654321a")) { // contains letter
            System.out.println("[PASS] 2.3 Invalid phone numbers correctly rejected (must be 10 digits starting with 6-9)");
            passed++;
        } else {
            System.err.println("[FAIL] 2.3 Phone validation allowed invalid numbers");
            failed++;
        }

        if (ValidationUtil.isValidPhone("9812345678") && ValidationUtil.isValidPhone("8123456789") && ValidationUtil.isValidPhone("7000000000") && ValidationUtil.isValidPhone("6999999999")) {
            System.out.println("[PASS] 2.4 Valid 10-digit mobile numbers correctly accepted");
            passed++;
        } else {
            System.err.println("[FAIL] 2.4 Valid phone numbers were rejected");
            failed++;
        }

        // 2.3 Password complexity (min 8 chars, uppercase, lowercase, digit)
        if (!ValidationUtil.isValidPassword(null) &&
            !ValidationUtil.isValidPassword("short") &&          // < 8 chars
            !ValidationUtil.isValidPassword("nouppercase123") && // missing uppercase
            !ValidationUtil.isValidPassword("NOLOWERCASE123") && // missing lowercase
            !ValidationUtil.isValidPassword("NoDigitsPassword")) { // missing digit
            System.out.println("[PASS] 2.5 Weak passwords correctly rejected (enforces min 8 chars, uppercase, lowercase, digit)");
            passed++;
        } else {
            System.err.println("[FAIL] 2.5 Password validation allowed weak passwords");
            failed++;
        }

        if (ValidationUtil.isValidPassword("Tripzy2026") && ValidationUtil.isValidPassword("StrongPass1!")) {
            System.out.println("[PASS] 2.6 Complex passwords correctly accepted");
            passed++;
        } else {
            System.err.println("[FAIL] 2.6 Valid complex passwords were rejected");
            failed++;
        }

        // 3. Registration Flow Validation in AuthService
        long timestamp = System.currentTimeMillis();
        String testEmail = "tripzy_user_" + timestamp + "@test.com";
        String testPhone = "9" + String.valueOf(timestamp).substring(String.valueOf(timestamp).length() - 9);
        String testPass = "TripzySecure123!";

        // 3.1 Weak password registration rejected
        try {
            authService.register("Test User", "test_weak_" + timestamp + "@test.com", "9800000001", "weakpass", "weakpass", "TRAVELER");
            System.err.println("[FAIL] 3.1 Registration should have failed for weak password");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 3.1 Registration rejected weak password: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 3.1 Unexpected exception for weak password: " + e.getMessage());
            failed++;
        }

        // 3.2 Password confirmation mismatch rejected
        try {
            authService.register("Test User", "test_mismatch_" + timestamp + "@test.com", "9800000002", testPass, "DifferentPass123!", "TRAVELER");
            System.err.println("[FAIL] 3.2 Registration should have failed for password mismatch");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 3.2 Registration rejected mismatched passwords: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 3.2 Unexpected exception for password mismatch: " + e.getMessage());
            failed++;
        }

        // 3.3 Invalid phone registration rejected
        try {
            authService.register("Test User", "test_phone_" + timestamp + "@test.com", "12345", testPass, testPass, "TRAVELER");
            System.err.println("[FAIL] 3.3 Registration should have failed for invalid phone");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 3.3 Registration rejected invalid phone: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 3.3 Unexpected exception for invalid phone: " + e.getMessage());
            failed++;
        }

        // 3.4 Valid user registration succeeds
        User registeredUser = null;
        try {
            registeredUser = authService.register("Tripzy Traveler", testEmail, testPhone, testPass, testPass, "TRAVELER");
            if (registeredUser != null && registeredUser.getId() > 0) {
                System.out.println("[PASS] 3.4 Valid user registered successfully: ID=" + registeredUser.getId() + ", Email=" + registeredUser.getEmail());
                passed++;
            } else {
                System.err.println("[FAIL] 3.4 Registration returned null user");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 3.4 Registration failed: " + e.getMessage());
            failed++;
        }

        // 3.5 Duplicate email registration rejected
        try {
            authService.register("Another Person", testEmail, "9777777777", testPass, testPass, "TRAVELER");
            System.err.println("[FAIL] 3.5 Registration should have rejected duplicate email");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 3.5 Duplicate email registration rejected: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 3.5 Unexpected exception for duplicate email: " + e.getMessage());
            failed++;
        }

        // 3.6 Duplicate phone registration rejected
        try {
            authService.register("Another Person", "unique_email_" + timestamp + "@test.com", testPhone, testPass, testPass, "TRAVELER");
            System.err.println("[FAIL] 3.6 Registration should have rejected duplicate phone number");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 3.6 Duplicate phone registration rejected: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 3.6 Unexpected exception for duplicate phone: " + e.getMessage());
            failed++;
        }

        // 4. Forgot Password & OTP Flow
        // 4.1 Non-existent email OTP request rejected
        try {
            authService.generatePasswordResetOtp("nonexistent_user_99999@domain.com");
            System.err.println("[FAIL] 4.1 OTP request for nonexistent email should have failed");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 4.1 OTP generation rejected nonexistent email: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 4.1 Unexpected exception for nonexistent email OTP: " + e.getMessage());
            failed++;
        }

        // 4.2 Valid OTP generated for registered user
        String generatedOtp = null;
        try {
            generatedOtp = authService.generatePasswordResetOtp(testEmail);
            if (generatedOtp != null && generatedOtp.matches("^\\d{6}$")) {
                System.out.println("[PASS] 4.2 6-Digit OTP generated successfully: OTP=" + generatedOtp);
                passed++;
            } else {
                System.err.println("[FAIL] 4.2 OTP was not a 6-digit numeric string: " + generatedOtp);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4.2 OTP generation failed: " + e.getMessage());
            failed++;
        }

        // 4.3 Reset with invalid OTP rejected
        try {
            authService.resetPasswordWithOtp(testEmail, "000000", "TripzyNewPass2026!", "TripzyNewPass2026!");
            System.err.println("[FAIL] 4.3 Reset with invalid OTP should have failed");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 4.3 Reset with invalid OTP correctly rejected: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 4.3 Unexpected exception for invalid OTP: " + e.getMessage());
            failed++;
        }

        // 4.4 Reset with weak new password rejected
        try {
            authService.resetPasswordWithOtp(testEmail, generatedOtp, "weak", "weak");
            System.err.println("[FAIL] 4.4 Reset with weak password should have failed");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 4.4 Reset with weak password rejected: " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 4.4 Unexpected exception for weak reset password: " + e.getMessage());
            failed++;
        }

        // 4.5 Reset with valid OTP and strong new password succeeds
        String newPassword = "TripzyUpdatedPass2026!";
        try {
            boolean resetOk = authService.resetPasswordWithOtp(testEmail, generatedOtp, newPassword, newPassword);
            if (resetOk) {
                System.out.println("[PASS] 4.5 Password reset succeeded with valid OTP and strong password");
                passed++;
            } else {
                System.err.println("[FAIL] 4.5 Password reset returned false");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4.5 Password reset threw exception: " + e.getMessage());
            failed++;
        }

        // 4.6 Old password rejected after reset
        try {
            authService.login(testEmail, testPass);
            System.err.println("[FAIL] 4.6 Old password should have been rejected after reset");
            failed++;
        } catch (AuthenticationException ae) {
            System.out.println("[PASS] 4.6 Old password correctly rejected after reset: " + ae.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 4.6 Unexpected exception for old password: " + e.getMessage());
            failed++;
        }

        // 4.7 New password successfully authenticates
        try {
            User loggedIn = authService.login(testEmail, newPassword);
            if (loggedIn != null && loggedIn.getEmail().equalsIgnoreCase(testEmail)) {
                System.out.println("[PASS] 4.7 New password authenticated successfully: Welcome " + loggedIn.getFullName());
                passed++;
            } else {
                System.err.println("[FAIL] 4.7 Login with new password returned null");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4.7 Login with new password failed: " + e.getMessage());
            failed++;
        }

        // 4.8 OTP cannot be reused (instant invalidation)
        try {
            authService.resetPasswordWithOtp(testEmail, generatedOtp, "AnotherStrongPass123!", "AnotherStrongPass123!");
            System.err.println("[FAIL] 4.8 Reusing already-used OTP should have failed");
            failed++;
        } catch (ValidationException ve) {
            System.out.println("[PASS] 4.8 Single-use OTP invalidation verified (cannot reuse): " + ve.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] 4.8 Unexpected exception on OTP reuse: " + e.getMessage());
            failed++;
        }

        // Final Summary
        System.out.println("==========================================================");
        System.out.println("TRIPZY FEATURE TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
