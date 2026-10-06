package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages flexible multi-step registration flow with in-memory pending state.
 * Supports:
 *   1. Name + Email + Password
 *   2. Name + Mobile + Password
 *   3. Name + Email + Mobile + Password
 * Enforces 6-digit OTP verification, 15-minute expiry, 60-second cooldown,
 * attempt limits, and single-use invalidation before creating active accounts in MySQL.
 */
public class PendingRegistrationManager {

    public static class PendingRegistration {
        private final String registrationId;
        private final String fullName;
        private final String email;
        private final String phone;
        private final String passwordHash;
        private final String role;

        private volatile String emailOtp;
        private volatile long emailOtpExpiry;
        private volatile boolean emailVerified;
        private volatile long lastEmailOtpSentTime;
        private volatile int emailAttempts;

        private volatile String mobileOtp;
        private volatile long mobileOtpExpiry;
        private volatile boolean mobileVerified;
        private volatile long lastMobileOtpSentTime;
        private volatile int mobileAttempts;

        private final long createdAt;

        public PendingRegistration(String registrationId, String fullName, String email,
                                   String phone, String passwordHash, String role,
                                   String emailOtp, long emailOtpExpiry,
                                   String mobileOtp, long mobileOtpExpiry) {
            this.registrationId = registrationId;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.passwordHash = passwordHash;
            this.role = role;

            this.emailOtp = emailOtp;
            this.emailOtpExpiry = emailOtpExpiry;
            this.emailVerified = false;
            this.lastEmailOtpSentTime = emailOtp != null ? System.currentTimeMillis() : 0;
            this.emailAttempts = 0;

            this.mobileOtp = mobileOtp;
            this.mobileOtpExpiry = mobileOtpExpiry;
            this.mobileVerified = false;
            this.lastMobileOtpSentTime = mobileOtp != null ? System.currentTimeMillis() : 0;
            this.mobileAttempts = 0;

            this.createdAt = System.currentTimeMillis();
        }

        // Backward compatibility constructor
        public PendingRegistration(String registrationId, String fullName, String email,
                                   String phone, String passwordHash, String role,
                                   String emailOtp, long emailOtpExpiry) {
            this(registrationId, fullName, email, phone, passwordHash, role, emailOtp, emailOtpExpiry, null, 0);
        }

        public String getRegistrationId() { return registrationId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getPasswordHash() { return passwordHash; }
        public String getRole() { return role; }

        public boolean hasEmail() { return ValidationUtil.isProvidedEmail(email); }
        public boolean hasPhone() { return ValidationUtil.isProvidedPhone(phone); }

        public boolean isEmailVerified() { return emailVerified; }
        public boolean isMobileVerified() { return mobileVerified; }

        public String getEmailOtp() { return emailOtp; }
        public String getMobileOtp() { return mobileOtp; }

        public int getEmailAttempts() { return emailAttempts; }
        public int getMobileAttempts() { return mobileAttempts; }

        public long getLastEmailOtpSentTime() { return lastEmailOtpSentTime; }
        public long getLastMobileOtpSentTime() { return lastMobileOtpSentTime; }

        public void setExpiredForTesting() {
            this.emailOtpExpiry = System.currentTimeMillis() - 1000;
            this.mobileOtpExpiry = System.currentTimeMillis() - 1000;
        }

        public void resetCooldownForTesting() {
            this.lastEmailOtpSentTime = System.currentTimeMillis() - 70000;
            this.lastMobileOtpSentTime = System.currentTimeMillis() - 70000;
        }

        public boolean isOverallExpired() {
            return System.currentTimeMillis() - createdAt > 30 * 60 * 1000; // 30 minutes total session
        }
    }

    private static final Map<String, PendingRegistration> pendingStore = new ConcurrentHashMap<>();
    private static final Map<String, String> contactToRegId = new ConcurrentHashMap<>();

    private static final long OTP_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes
    private static final long RESEND_COOLDOWN_MS = 60 * 1000;  // 60 seconds
    private static final int MAX_ATTEMPTS = 5;

    public static String generateOtp() {
        int num = 100000 + new Random().nextInt(900000);
        return String.valueOf(num);
    }

    /**
     * Validates account inputs and initiates pending registration.
     * Enforces that at least one of Email OR Mobile is provided.
     */
    public static PendingRegistration initiateRegistration(
            String fullName, String email, String phone,
            String password, String confirmPassword, String role,
            boolean termsAccepted, UserDAO userDAO)
            throws ValidationException, DatabaseException {

        if (!termsAccepted) {
            throw new ValidationException("You must accept the Terms of Service and Privacy Policy to proceed.");
        }
        if (!ValidationUtil.isValidName(fullName)) {
            throw new ValidationException("Full Name is required and must contain only letters, numbers, and spaces (minimum 2 characters).");
        }

        boolean hasEmail = ValidationUtil.isNotEmpty(email);
        boolean hasPhone = ValidationUtil.isNotEmpty(phone);

        if (!hasEmail && !hasPhone) {
            throw new ValidationException("Please provide either an email address or mobile number.");
        }

        String normEmail = null;
        if (hasEmail) {
            if (!ValidationUtil.isValidEmail(email)) {
                throw new ValidationException("Please enter a valid email address.");
            }
            normEmail = email.trim();
        }

        String cleanPhone = null;
        if (hasPhone) {
            if (!ValidationUtil.isValidPhone(phone)) {
                throw new ValidationException("Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.");
            }
            cleanPhone = ValidationUtil.cleanPhone(phone);
        }

        String targetRole = "TRAVELER";
        if ("ADMIN".equalsIgnoreCase(role)) {
            throw new ValidationException("Administrator accounts cannot be registered directly. Please contact system support.");
        } else if ("AGENT".equalsIgnoreCase(role)) {
            targetRole = "AGENT";
        }

        if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        try {
            // Check database for duplicate email if provided
            if (normEmail != null) {
                User existingEmail = userDAO.findByEmail(normEmail);
                if (existingEmail != null) {
                    throw new ValidationException("An account with this email already exists. Please login.");
                }
            }

            // Check database for duplicate phone if provided
            if (cleanPhone != null) {
                User existingPhone = userDAO.findByPhone(cleanPhone);
                if (existingPhone != null) {
                    throw new ValidationException("This mobile number is already registered.");
                }
            }

            String regId = UUID.randomUUID().toString();
            String emailOtp = null;
            long emailExpiry = 0;
            if (hasEmail) {
                emailOtp = generateOtp();
                emailExpiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
            }

            String mobileOtp = null;
            long mobileExpiry = 0;
            if (hasPhone) {
                mobileOtp = generateOtp();
                mobileExpiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
            }

            String passwordHash = PasswordUtil.hashPassword(password);

            PendingRegistration pending = new PendingRegistration(
                    regId, fullName.trim(), normEmail, cleanPhone, passwordHash, targetRole,
                    emailOtp, emailExpiry, mobileOtp, mobileExpiry
            );

            // Clean up any earlier pending attempt for this email or phone
            if (normEmail != null) {
                String oldRegId = contactToRegId.get(normEmail.toLowerCase());
                if (oldRegId != null) pendingStore.remove(oldRegId);
                contactToRegId.put(normEmail.toLowerCase(), regId);
            }
            if (cleanPhone != null) {
                String oldRegId = contactToRegId.get(cleanPhone);
                if (oldRegId != null) pendingStore.remove(oldRegId);
                contactToRegId.put(cleanPhone, regId);
            }

            pendingStore.put(regId, pending);
            return pending;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during registration check: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies registration OTP (email or mobile) and activates the account in database.
     * When both email and mobile are provided, verifying either activates the account.
     */
    public static User verifyRegistrationOtp(String registrationId, String otp, UserDAO userDAO)
            throws ValidationException, DatabaseException {
        if (registrationId == null || !pendingStore.containsKey(registrationId)) {
            throw new ValidationException("Registration session expired or not found. Please start registration again.");
        }
        PendingRegistration pending = pendingStore.get(registrationId);
        if (pending.isOverallExpired()) {
            removePending(pending);
            throw new ValidationException("Registration session has expired. Please start registration again.");
        }

        if (otp == null || otp.trim().isEmpty()) {
            throw new ValidationException("Please enter the 6-digit verification code.");
        }
        String cleanOtp = otp.trim();

        boolean emailMatch = pending.emailOtp != null && pending.emailOtp.equals(cleanOtp);
        boolean mobileMatch = pending.mobileOtp != null && pending.mobileOtp.equals(cleanOtp);

        // Check expiry for matched OTP
        if (emailMatch && System.currentTimeMillis() > pending.emailOtpExpiry) {
            throw new ValidationException("Verification code has expired (15-minute limit). Please request a new code.");
        }
        if (mobileMatch && System.currentTimeMillis() > pending.mobileOtpExpiry) {
            throw new ValidationException("Verification code has expired (15-minute limit). Please request a new code.");
        }

        if (!emailMatch && !mobileMatch) {
            // Determine which attempt to increment
            if (pending.emailOtp != null) pending.emailAttempts++;
            if (pending.mobileOtp != null) pending.mobileAttempts++;

            int maxAttempts = Math.max(pending.emailAttempts, pending.mobileAttempts);
            int remaining = MAX_ATTEMPTS - maxAttempts;
            if (remaining > 0) {
                throw new ValidationException("Incorrect verification code. (" + remaining + " attempts remaining)");
            } else {
                throw new ValidationException("Too many failed attempts. Please request a new verification code.");
            }
        }

        if (emailMatch) {
            pending.emailVerified = true;
            pending.emailOtp = null; // single-use
        }
        if (mobileMatch) {
            pending.mobileVerified = true;
            pending.mobileOtp = null; // single-use
        }

        return activatePendingUser(pending, userDAO);
    }

    /**
     * Verifies email OTP specifically. If pending user has mobile and it's not yet verified,
     * keeps pending session for mobile verification (matching 2-step test flow),
     * or activates if email was the only provided contact.
     */
    public static PendingRegistration verifyEmailOtp(String registrationId, String otp)
            throws ValidationException {
        if (registrationId == null || !pendingStore.containsKey(registrationId)) {
            throw new ValidationException("Registration session expired or not found. Please start registration again.");
        }
        PendingRegistration pending = pendingStore.get(registrationId);
        if (pending.isOverallExpired()) {
            removePending(pending);
            throw new ValidationException("Registration session has expired. Please start registration again.");
        }
        if (pending.emailVerified) {
            return pending;
        }
        if (pending.emailOtp == null || System.currentTimeMillis() > pending.emailOtpExpiry) {
            throw new ValidationException("Email verification code has expired (15-minute limit). Please request a new code.");
        }
        if (pending.emailAttempts >= MAX_ATTEMPTS) {
            throw new ValidationException("Too many failed attempts. Please request a new verification code.");
        }
        if (otp == null || !pending.emailOtp.equals(otp.trim())) {
            pending.emailAttempts++;
            int remaining = MAX_ATTEMPTS - pending.emailAttempts;
            if (remaining > 0) {
                throw new ValidationException("Incorrect email verification code. (" + remaining + " attempts remaining)");
            } else {
                throw new ValidationException("Too many failed attempts. Please request a new verification code.");
            }
        }

        // Email OTP is single-use: mark verified and consume OTP
        pending.emailVerified = true;
        pending.emailOtp = null;

        // If mobile is present and doesn't have an active OTP yet, generate it
        if (pending.hasPhone() && pending.mobileOtp == null && !pending.mobileVerified) {
            pending.mobileOtp = generateOtp();
            pending.mobileOtpExpiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
            pending.lastMobileOtpSentTime = System.currentTimeMillis();
            pending.mobileAttempts = 0;
        }

        return pending;
    }

    /**
     * Verifies mobile OTP and activates account in database.
     */
    public static User verifyMobileOtpAndActivate(String registrationId, String otp, UserDAO userDAO)
            throws ValidationException, DatabaseException {
        if (registrationId == null || !pendingStore.containsKey(registrationId)) {
            throw new ValidationException("Registration session expired or not found. Please start registration again.");
        }
        PendingRegistration pending = pendingStore.get(registrationId);
        if (pending.isOverallExpired()) {
            removePending(pending);
            throw new ValidationException("Registration session has expired. Please start registration again.");
        }
        if (pending.mobileVerified) {
            throw new ValidationException("Account has already been activated. Please log in.");
        }
        if (pending.mobileOtp == null || System.currentTimeMillis() > pending.mobileOtpExpiry) {
            throw new ValidationException("Mobile verification code has expired (15-minute limit). Please request a new code.");
        }
        if (pending.mobileAttempts >= MAX_ATTEMPTS) {
            throw new ValidationException("Too many failed attempts. Please request a new verification code.");
        }
        if (otp == null || !pending.mobileOtp.equals(otp.trim())) {
            pending.mobileAttempts++;
            int remaining = MAX_ATTEMPTS - pending.mobileAttempts;
            if (remaining > 0) {
                throw new ValidationException("Incorrect mobile verification code. (" + remaining + " attempts remaining)");
            } else {
                throw new ValidationException("Too many failed attempts. Please request a new verification code.");
            }
        }

        // Mobile OTP is single-use: mark verified and consume OTP
        pending.mobileVerified = true;
        pending.mobileOtp = null;

        return activatePendingUser(pending, userDAO);
    }

    /**
     * Creates active user in MySQL database and cleans up pending state.
     */
    private static User activatePendingUser(PendingRegistration pending, UserDAO userDAO)
            throws ValidationException, DatabaseException {
        try {
            // Determine DB-stored values:
            // 1. If email was provided, use it. If not, generate a safe unique placeholder email for MySQL NOT NULL UNIQUE constraint.
            String dbEmail;
            if (pending.hasEmail()) {
                dbEmail = pending.getEmail();
                if (userDAO.findByEmail(dbEmail) != null) {
                    throw new ValidationException("An account with this email already exists. Please login.");
                }
            } else {
                dbEmail = ValidationUtil.generatePlaceholderEmail(pending.getPhone());
            }

            // 2. If phone was provided, use it. If not, use empty string for MySQL NOT NULL constraint.
            String dbPhone = pending.hasPhone() ? pending.getPhone() : "";
            if (pending.hasPhone() && userDAO.findByPhone(dbPhone) != null) {
                throw new ValidationException("This mobile number is already registered.");
            }

            User newUser = new User(0, pending.getFullName(), dbEmail, dbPhone, pending.getPasswordHash(), pending.getRole(), "ACTIVE");
            boolean created = userDAO.save(newUser);
            if (!created) {
                throw new DatabaseException("Failed to activate user account. Please try again.", null);
            }

            // Record verification status in AuthService tracker
            AuthService.setVerificationStatus(newUser.getId(), pending.isEmailVerified(), pending.isMobileVerified());

            // Clean up from pending store
            removePending(pending);

            return newUser;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during account activation: " + e.getMessage(), e);
        }
    }

    private static void removePending(PendingRegistration pending) {
        if (pending == null) return;
        pendingStore.remove(pending.getRegistrationId());
        if (pending.getEmail() != null) {
            contactToRegId.remove(pending.getEmail().toLowerCase());
        }
        if (pending.getPhone() != null) {
            contactToRegId.remove(pending.getPhone());
        }
    }

    /**
     * Resends email OTP subject to 60-second cooldown.
     */
    public static String resendEmailOtp(String registrationId) throws ValidationException {
        if (registrationId == null || !pendingStore.containsKey(registrationId)) {
            throw new ValidationException("Registration session expired or not found. Please start registration again.");
        }
        PendingRegistration pending = pendingStore.get(registrationId);
        if (!pending.hasEmail()) {
            throw new ValidationException("No email address provided for this registration.");
        }
        if (pending.emailVerified) {
            throw new ValidationException("Email is already verified.");
        }
        long elapsed = System.currentTimeMillis() - pending.lastEmailOtpSentTime;
        if (elapsed < RESEND_COOLDOWN_MS) {
            long remainingSec = (RESEND_COOLDOWN_MS - elapsed) / 1000 + 1;
            throw new ValidationException("Please wait " + remainingSec + " seconds before requesting a new code.");
        }

        pending.emailOtp = generateOtp();
        pending.emailOtpExpiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
        pending.lastEmailOtpSentTime = System.currentTimeMillis();
        pending.emailAttempts = 0;
        return pending.emailOtp;
    }

    /**
     * Resends mobile OTP subject to 60-second cooldown.
     */
    public static String resendMobileOtp(String registrationId) throws ValidationException {
        if (registrationId == null || !pendingStore.containsKey(registrationId)) {
            throw new ValidationException("Registration session expired or not found. Please start registration again.");
        }
        PendingRegistration pending = pendingStore.get(registrationId);
        if (!pending.hasPhone()) {
            throw new ValidationException("No mobile number provided for this registration.");
        }
        if (pending.mobileVerified) {
            throw new ValidationException("Mobile number is already verified.");
        }
        long elapsed = System.currentTimeMillis() - pending.lastMobileOtpSentTime;
        if (elapsed < RESEND_COOLDOWN_MS) {
            long remainingSec = (RESEND_COOLDOWN_MS - elapsed) / 1000 + 1;
            throw new ValidationException("Please wait " + remainingSec + " seconds before requesting a new code.");
        }

        pending.mobileOtp = generateOtp();
        pending.mobileOtpExpiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
        pending.lastMobileOtpSentTime = System.currentTimeMillis();
        pending.mobileAttempts = 0;
        return pending.mobileOtp;
    }

    public static PendingRegistration getPending(String registrationId) {
        if (registrationId == null) return null;
        return pendingStore.get(registrationId);
    }

    public static void clear() {
        pendingStore.clear();
        contactToRegId.clear();
    }
}
