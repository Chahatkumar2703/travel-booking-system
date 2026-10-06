package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.SessionManager;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing user authentication, registration, password recovery, and profile operations.
 */
public class AuthService {

    private final UserDAO userDAO;

    public static class VerificationStatus {
        private final boolean emailVerified;
        private final boolean mobileVerified;

        public VerificationStatus(boolean emailVerified, boolean mobileVerified) {
            this.emailVerified = emailVerified;
            this.mobileVerified = mobileVerified;
        }

        public boolean isEmailVerified() { return emailVerified; }
        public boolean isMobileVerified() { return mobileVerified; }
    }

    private static final Map<Integer, VerificationStatus> verificationStatusMap = new ConcurrentHashMap<>();

    private static class PendingContactUpdate {
        final int userId;
        final String newContact;
        final String otp;
        final long expiry;
        final long sentTime;
        int attempts;

        PendingContactUpdate(int userId, String newContact, String otp, long expiry) {
            this.userId = userId;
            this.newContact = newContact;
            this.otp = otp;
            this.expiry = expiry;
            this.sentTime = System.currentTimeMillis();
            this.attempts = 0;
        }
    }

    private static final Map<Integer, PendingContactUpdate> pendingEmailUpdates = new ConcurrentHashMap<>();
    private static final Map<Integer, PendingContactUpdate> pendingMobileUpdates = new ConcurrentHashMap<>();
    private static final long PROFILE_OTP_EXPIRY_MS = 15 * 60 * 1000; // 15 mins
    private static final long PROFILE_RESEND_COOLDOWN_MS = 60 * 1000; // 60 secs
    private static final int MAX_ATTEMPTS = 5;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public static void setVerificationStatus(int userId, boolean emailVerified, boolean mobileVerified) {
        verificationStatusMap.put(userId, new VerificationStatus(emailVerified, mobileVerified));
    }

    public static VerificationStatus getVerificationStatus(User user) {
        if (user == null) return new VerificationStatus(false, false);
        VerificationStatus vs = verificationStatusMap.get(user.getId());
        if (vs != null) {
            return vs;
        }
        boolean ev = ValidationUtil.isProvidedEmail(user.getEmail());
        boolean mv = ValidationUtil.isProvidedPhone(user.getPhone());
        return new VerificationStatus(ev, mv);
    }

    /**
     * Authenticates user with registered email OR mobile number and password.
     */
    public User login(String emailOrPhone, String password) throws AuthenticationException, DatabaseException {
        if (!ValidationUtil.isNotEmpty(emailOrPhone) || !ValidationUtil.isNotEmpty(password)) {
            throw new AuthenticationException("Please provide both email/username and password.");
        }

        try {
            User user = null;
            String trimmed = emailOrPhone.trim();
            if (trimmed.contains("@")) {
                user = userDAO.findByEmail(trimmed);
                if (user == null) {
                    throw new AuthenticationException("Account not found with this email address.");
                }
            } else {
                String clean = ValidationUtil.cleanPhone(trimmed);
                if (!clean.isEmpty()) {
                    user = userDAO.findByPhone(clean);
                }
                if (user == null) {
                    user = userDAO.findByEmail(trimmed);
                }
                if (user == null) {
                    throw new AuthenticationException("Account not found with this mobile number.");
                }
            }

            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                throw new AuthenticationException("Incorrect password. Please try again.");
            }

            if (!user.isActive()) {
                throw new AuthenticationException("Your account has been deactivated by administrator.");
            }

            SessionManager.setCurrentUser(user);
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new customer or travel agent account directly (Desktop/legacy).
     */
    public User register(String fullName, String email, String phone,
                         String password, String confirmPassword)
            throws ValidationException, DatabaseException {
        return register(fullName, email, phone, password, confirmPassword, "TRAVELER");
    }

    public User register(String fullName, String email, String phone,
                         String password, String confirmPassword, String role)
            throws ValidationException, DatabaseException {

        if (!ValidationUtil.isValidName(fullName)) {
            throw new ValidationException("Full Name is required and must contain only letters, numbers, and spaces (minimum 2 characters).");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new ValidationException("Please enter a valid 10-digit mobile number.");
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

        String cleanPhone = ValidationUtil.cleanPhone(phone);

        try {
            User existingEmail = userDAO.findByEmail(email.trim());
            if (existingEmail != null) {
                throw new ValidationException("An account with this email already exists. Please login.");
            }

            User existingPhone = userDAO.findByPhone(cleanPhone);
            if (existingPhone != null) {
                throw new ValidationException("This mobile number is already registered.");
            }

            String passwordHash = PasswordUtil.hashPassword(password);
            User newUser = new User(0, fullName.trim(), email.trim(), cleanPhone, passwordHash, targetRole, "ACTIVE");
            boolean created = userDAO.save(newUser);

            if (!created) {
                throw new DatabaseException("Failed to register user. Please try again.", null);
            }

            setVerificationStatus(newUser.getId(), true, true);
            return newUser;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during registration: " + e.getMessage(), e);
        }
    }

    /**
     * Initiates flexible registration (Email only, Mobile only, or both) with OTP generation.
     */
    public PendingRegistrationManager.PendingRegistration initiateRegistration(
            String fullName, String email, String phone,
            String password, String confirmPassword, String role, boolean termsAccepted)
            throws ValidationException, DatabaseException {
        return PendingRegistrationManager.initiateRegistration(
                fullName, email, phone, password, confirmPassword, role, termsAccepted, userDAO
        );
    }

    /**
     * Verifies registration OTP (email or mobile) and activates the account in database.
     */
    public User verifyRegistrationOtp(String registrationId, String otp)
            throws ValidationException, DatabaseException {
        return PendingRegistrationManager.verifyRegistrationOtp(registrationId, otp, userDAO);
    }

    /**
     * Verifies email OTP specifically (marks email verified, prepares mobile step).
     */
    public PendingRegistrationManager.PendingRegistration verifyEmailRegistrationOtp(String registrationId, String otp)
            throws ValidationException {
        return PendingRegistrationManager.verifyEmailOtp(registrationId, otp);
    }

    /**
     * Resends email OTP subject to cooldown.
     */
    public String resendEmailRegistrationOtp(String registrationId) throws ValidationException {
        return PendingRegistrationManager.resendEmailOtp(registrationId);
    }

    /**
     * Verifies mobile OTP and activates account in database.
     */
    public User verifyMobileRegistrationOtpAndActivate(String registrationId, String otp)
            throws ValidationException, DatabaseException {
        return PendingRegistrationManager.verifyMobileOtpAndActivate(registrationId, otp, userDAO);
    }

    /**
     * Resends mobile OTP subject to cooldown.
     */
    public String resendMobileRegistrationOtp(String registrationId) throws ValidationException {
        return PendingRegistrationManager.resendMobileOtp(registrationId);
    }

    /**
     * Generates a 6-digit OTP for password reset (accepts Email OR Mobile).
     */
    public String generatePasswordResetOtp(String emailOrPhone) throws ValidationException, DatabaseException {
        return PasswordResetManager.generateResetOtp(emailOrPhone, userDAO);
    }

    /**
     * Resets user password using the 6-digit OTP (accepts Email OR Mobile).
     */
    public boolean resetPasswordWithOtp(String emailOrPhone, String otp, String newPassword, String confirmPassword)
            throws ValidationException, DatabaseException {
        return PasswordResetManager.resetPasswordWithOtp(emailOrPhone, otp, newPassword, confirmPassword, userDAO);
    }

    /**
     * Updates user's display name.
     */
    public boolean updateProfileName(int userId, String fullName) throws ValidationException, DatabaseException {
        if (!ValidationUtil.isValidName(fullName)) {
            throw new ValidationException("Full Name is required and must contain only letters, numbers, and spaces (minimum 2 characters).");
        }
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User account not found.");
            }
            boolean ok = userDAO.updateName(userId, fullName.trim());
            if (ok && SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
                SessionManager.getCurrentUser().setFullName(fullName.trim());
            }
            return ok;
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating name: " + e.getMessage(), e);
        }
    }

    /**
     * Requests OTP for adding or changing email address.
     */
    public String requestEmailChangeOtp(int userId, String newEmail) throws ValidationException, DatabaseException {
        if (!ValidationUtil.isValidEmail(newEmail)) {
            throw new ValidationException("Please enter a valid email address.");
        }
        String cleanEmail = newEmail.trim().toLowerCase();
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User account not found.");
            }
            User existing = userDAO.findByEmail(cleanEmail);
            if (existing != null && existing.getId() != userId) {
                throw new ValidationException("An account with this email already exists. Please choose a different email.");
            }

            PendingContactUpdate prev = pendingEmailUpdates.get(userId);
            if (prev != null) {
                long elapsed = System.currentTimeMillis() - prev.sentTime;
                if (elapsed < PROFILE_RESEND_COOLDOWN_MS) {
                    long remainingSec = (PROFILE_RESEND_COOLDOWN_MS - elapsed) / 1000 + 1;
                    throw new ValidationException("Please wait " + remainingSec + " seconds before requesting a new code.");
                }
            }

            int num = 100000 + new Random().nextInt(900000);
            String otp = String.valueOf(num);
            long expiry = System.currentTimeMillis() + PROFILE_OTP_EXPIRY_MS;
            pendingEmailUpdates.put(userId, new PendingContactUpdate(userId, cleanEmail, otp, expiry));

            return otp;
        } catch (SQLException e) {
            throw new DatabaseException("Database error requesting email OTP: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies OTP and saves the new email address.
     */
    public boolean verifyEmailChangeOtp(int userId, String newEmail, String otp) throws ValidationException, DatabaseException {
        if (otp == null || otp.trim().length() != 6) {
            throw new ValidationException("Please enter a valid 6-digit verification code.");
        }
        PendingContactUpdate pending = pendingEmailUpdates.get(userId);
        if (pending == null) {
            throw new ValidationException("No active email verification request found. Please request a new code.");
        }
        if (System.currentTimeMillis() > pending.expiry) {
            pendingEmailUpdates.remove(userId);
            throw new ValidationException("Verification code has expired (15-minute limit). Please request a new code.");
        }
        if (pending.attempts >= MAX_ATTEMPTS) {
            pendingEmailUpdates.remove(userId);
            throw new ValidationException("Too many failed attempts. Please request a new verification code.");
        }
        if (!pending.otp.equals(otp.trim())) {
            pending.attempts++;
            int remaining = MAX_ATTEMPTS - pending.attempts;
            if (remaining > 0) {
                throw new ValidationException("Incorrect verification code. (" + remaining + " attempts remaining)");
            } else {
                pendingEmailUpdates.remove(userId);
                throw new ValidationException("Too many failed attempts. Please request a new verification code.");
            }
        }

        // Single-use: consume pending
        pendingEmailUpdates.remove(userId);

        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User account not found.");
            }

            boolean ok = userDAO.updateEmail(userId, pending.newContact);
            if (ok) {
                VerificationStatus vs = getVerificationStatus(user);
                setVerificationStatus(userId, true, vs.isMobileVerified());

                if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
                    SessionManager.getCurrentUser().setEmail(pending.newContact);
                }
            }
            return ok;
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating email: " + e.getMessage(), e);
        }
    }

    /**
     * Requests OTP for adding or changing mobile number.
     */
    public String requestMobileChangeOtp(int userId, String newPhone) throws ValidationException, DatabaseException {
        if (!ValidationUtil.isValidPhone(newPhone)) {
            throw new ValidationException("Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.");
        }
        String cleanPhone = ValidationUtil.cleanPhone(newPhone);
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User account not found.");
            }
            User existing = userDAO.findByPhone(cleanPhone);
            if (existing != null && existing.getId() != userId) {
                throw new ValidationException("This mobile number is already registered to another account.");
            }

            PendingContactUpdate prev = pendingMobileUpdates.get(userId);
            if (prev != null) {
                long elapsed = System.currentTimeMillis() - prev.sentTime;
                if (elapsed < PROFILE_RESEND_COOLDOWN_MS) {
                    long remainingSec = (PROFILE_RESEND_COOLDOWN_MS - elapsed) / 1000 + 1;
                    throw new ValidationException("Please wait " + remainingSec + " seconds before requesting a new code.");
                }
            }

            int num = 100000 + new Random().nextInt(900000);
            String otp = String.valueOf(num);
            long expiry = System.currentTimeMillis() + PROFILE_OTP_EXPIRY_MS;
            pendingMobileUpdates.put(userId, new PendingContactUpdate(userId, cleanPhone, otp, expiry));

            return otp;
        } catch (SQLException e) {
            throw new DatabaseException("Database error requesting mobile OTP: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies OTP and saves the new mobile number.
     */
    public boolean verifyMobileChangeOtp(int userId, String newPhone, String otp) throws ValidationException, DatabaseException {
        if (otp == null || otp.trim().length() != 6) {
            throw new ValidationException("Please enter a valid 6-digit verification code.");
        }
        PendingContactUpdate pending = pendingMobileUpdates.get(userId);
        if (pending == null) {
            throw new ValidationException("No active mobile verification request found. Please request a new code.");
        }
        if (System.currentTimeMillis() > pending.expiry) {
            pendingMobileUpdates.remove(userId);
            throw new ValidationException("Verification code has expired (15-minute limit). Please request a new code.");
        }
        if (pending.attempts >= MAX_ATTEMPTS) {
            pendingMobileUpdates.remove(userId);
            throw new ValidationException("Too many failed attempts. Please request a new verification code.");
        }
        if (!pending.otp.equals(otp.trim())) {
            pending.attempts++;
            int remaining = MAX_ATTEMPTS - pending.attempts;
            if (remaining > 0) {
                throw new ValidationException("Incorrect verification code. (" + remaining + " attempts remaining)");
            } else {
                pendingMobileUpdates.remove(userId);
                throw new ValidationException("Too many failed attempts. Please request a new verification code.");
            }
        }

        // Single-use: consume pending
        pendingMobileUpdates.remove(userId);

        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User account not found.");
            }

            boolean ok = userDAO.updatePhone(userId, pending.newContact);
            if (ok) {
                VerificationStatus vs = getVerificationStatus(user);
                setVerificationStatus(userId, vs.isEmailVerified(), true);

                if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
                    SessionManager.getCurrentUser().setPhone(pending.newContact);
                }
            }
            return ok;
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating mobile: " + e.getMessage(), e);
        }
    }

    /**
     * Safety check: ensures user cannot remove/replace their only contact method leaving them with neither email nor mobile.
     */
    public void validateContactRemovalSafety(User user, boolean removingEmail, boolean removingPhone) throws ValidationException {
        if (user == null) return;
        boolean hasEmail = ValidationUtil.isProvidedEmail(user.getEmail());
        boolean hasPhone = ValidationUtil.isProvidedPhone(user.getPhone());

        if (removingEmail && !hasPhone) {
            throw new ValidationException("Cannot remove email address as it is your only registered contact method.");
        }
        if (removingPhone && !hasEmail) {
            throw new ValidationException("Cannot remove mobile number as it is your only registered contact method.");
        }
    }

    /**
     * Legacy profile update method.
     */
    public boolean updateProfile(int userId, String fullName, String phone)
            throws ValidationException, DatabaseException {
        if (!ValidationUtil.isNotEmpty(fullName)) {
            throw new ValidationException("Full Name cannot be empty.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new ValidationException("Please enter a valid phone number.");
        }

        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User not found.");
            }
            user.setFullName(fullName.trim());
            user.setPhone(ValidationUtil.cleanPhone(phone));

            boolean updated = userDAO.update(user);
            if (updated && SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
                SessionManager.getCurrentUser().setFullName(fullName.trim());
                SessionManager.getCurrentUser().setPhone(user.getPhone());
            }
            return updated;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating profile: " + e.getMessage(), e);
        }
    }

    /**
     * Changes user account password.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword, String confirmNewPassword)
            throws ValidationException, AuthenticationException, DatabaseException {
        if (!ValidationUtil.isNotEmpty(oldPassword)) {
            throw new ValidationException("Current password is required.");
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new ValidationException("New password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new ValidationException("New passwords do not match.");
        }

        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User not found.");
            }

            if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Current password entered is incorrect.");
            }

            String newHash = PasswordUtil.hashPassword(newPassword);
            return userDAO.updatePassword(userId, newHash);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password: " + e.getMessage(), e);
        }
    }

    public double[] getUserStats(int userId) throws DatabaseException {
        try {
            return userDAO.getUserStats(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error loading profile stats: " + e.getMessage(), e);
        }
    }
}
