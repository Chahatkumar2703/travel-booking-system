package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.SessionManager;
import util.ValidationUtil;

import java.sql.SQLException;

/**
 * Service managing user authentication, registration, and profile operations.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates user with email and password.
     */
    public User login(String email, String password) throws AuthenticationException, DatabaseException {
        if (!ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(password)) {
            throw new AuthenticationException("Please provide both email/username and password.");
        }

        try {
            User user = userDAO.findByEmail(email.trim());
            if (user == null) {
                throw new AuthenticationException("Account not found with this email address.");
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
     * Registers a new customer account.
     */
    public User register(String fullName, String email, String phone,
                         String password, String confirmPassword)
            throws ValidationException, DatabaseException {

        // Validate empty fields
        if (!ValidationUtil.isNotEmpty(fullName)) {
            throw new ValidationException("Full Name is required.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new ValidationException("Please enter a valid 10-digit phone number.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        try {
            // Duplicate email check
            User existing = userDAO.findByEmail(email.trim());
            if (existing != null) {
                throw new ValidationException("An account already exists with this email address.");
            }

            String passwordHash = PasswordUtil.hashPassword(password);
            User newUser = new User(0, fullName.trim(), email.trim(), phone.trim(), passwordHash, "USER", "ACTIVE");
            boolean created = userDAO.save(newUser);

            if (!created) {
                throw new DatabaseException("Failed to register user. Please try again.", null);
            }

            return newUser;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during registration: " + e.getMessage(), e);
        }
    }

    /**
     * Updates user personal profile information.
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
            user.setPhone(phone.trim());

            boolean updated = userDAO.update(user);
            if (updated && SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getId() == userId) {
                SessionManager.getCurrentUser().setFullName(fullName.trim());
                SessionManager.getCurrentUser().setPhone(phone.trim());
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
            throw new ValidationException("New password must be at least 6 characters long.");
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
