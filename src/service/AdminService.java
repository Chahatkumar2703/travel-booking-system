package service;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import model.AdminStats;
import model.Booking;
import model.Payment;
import model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Service aggregating administrative reports, user moderation, and financial logs.
 */
public class AdminService {

    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;
    private final BookingDAO bookingDAO;

    public AdminService() {
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        this.bookingDAO = new BookingDAO();
    }

    public AdminStats getDashboardStats() throws DatabaseException {
        try {
            return paymentDAO.getAdminStats();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load dashboard metrics: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() throws DatabaseException {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load users: " + e.getMessage(), e);
        }
    }

    public List<User> searchUsers(String query) throws DatabaseException {
        try {
            if (query == null || query.trim().isEmpty()) {
                return userDAO.findAll();
            }
            return userDAO.searchUsers(query.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Failed searching users: " + e.getMessage(), e);
        }
    }

    public boolean toggleUserStatus(int userId, String status) throws DatabaseException {
        try {
            return userDAO.toggleStatus(userId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Failed updating user status: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAllBookings() throws DatabaseException {
        try {
            return bookingDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load bookings: " + e.getMessage(), e);
        }
    }

    public boolean updateBookingStatus(int bookingId, String status) throws DatabaseException {
        try {
            return bookingDAO.updateStatus(bookingId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Failed updating booking status: " + e.getMessage(), e);
        }
    }

    public List<Payment> getAllPayments() throws DatabaseException {
        try {
            return paymentDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load payments: " + e.getMessage(), e);
        }
    }
}
