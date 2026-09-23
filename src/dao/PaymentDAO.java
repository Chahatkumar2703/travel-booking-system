package dao;

import model.AdminStats;
import model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment entities and financial statistics.
 */
public class PaymentDAO extends BaseDAO implements GenericDAO<Payment> {

    private static final String BASE_SELECT =
            "SELECT p.*, b.booking_code, u.full_name AS user_name " +
            "FROM payments p " +
            "JOIN bookings b ON p.booking_id = b.id " +
            "JOIN users u ON p.user_id = u.id ";

    @Override
    public Payment findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Payment findByBookingId(int bookingId) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.booking_id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY p.id DESC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean save(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (transaction_code, booking_id, user_id, amount, " +
                "payment_method, payment_details, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTransactionCode());
            ps.setInt(2, p.getBookingId());
            ps.setInt(3, p.getUserId());
            ps.setDouble(4, p.getAmount());
            ps.setString(5, p.getPaymentMethod());
            ps.setString(6, p.getPaymentDetails());
            ps.setString(7, p.getPaymentStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Payment p) throws SQLException {
        String sql = "UPDATE payments SET payment_status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPaymentStatus());
            ps.setInt(2, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM payments WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Aggregates key platform statistics for the Admin Dashboard.
     */
    public AdminStats getAdminStats() throws SQLException {
        AdminStats stats = new AdminStats();
        String sql = "SELECT " +
                "  (SELECT COUNT(*) FROM users WHERE role = 'USER') AS total_users, " +
                "  (SELECT COUNT(*) FROM destinations) AS total_destinations, " +
                "  (SELECT COUNT(*) FROM packages) AS total_packages, " +
                "  (SELECT COUNT(*) FROM hotels) AS total_hotels, " +
                "  (SELECT COUNT(*) FROM bookings) AS total_bookings, " +
                "  (SELECT COUNT(*) FROM bookings WHERE booking_status = 'CONFIRMED') AS confirmed_bookings, " +
                "  (SELECT COUNT(*) FROM bookings WHERE booking_status = 'CANCELLED') AS cancelled_bookings, " +
                "  (SELECT COALESCE(SUM(total_amount), 0) FROM bookings WHERE booking_status = 'CONFIRMED') AS total_revenue;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.setTotalUsers(rs.getInt("total_users"));
                stats.setTotalDestinations(rs.getInt("total_destinations"));
                stats.setTotalPackages(rs.getInt("total_packages"));
                stats.setTotalHotels(rs.getInt("total_hotels"));
                stats.setTotalBookings(rs.getInt("total_bookings"));
                stats.setConfirmedBookings(rs.getInt("confirmed_bookings"));
                stats.setCancelledBookings(rs.getInt("cancelled_bookings"));
                stats.setTotalRevenue(rs.getDouble("total_revenue"));
            }
        }
        return stats;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment(
                rs.getInt("id"),
                rs.getString("transaction_code"),
                rs.getInt("booking_id"),
                rs.getString("booking_code"),
                rs.getInt("user_id"),
                rs.getString("user_name"),
                rs.getDouble("amount"),
                rs.getString("payment_method"),
                rs.getString("payment_details"),
                rs.getString("payment_status"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
        );
        return p;
    }
}
