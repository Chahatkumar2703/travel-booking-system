package dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities.
 */
public class UserDAO extends BaseDAO implements GenericDAO<User> {

    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?;";
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

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<User> searchUsers(String query) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE full_name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY id ASC;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + query.trim() + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean save(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role, status) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean toggleStatus(int userId, String newStatus) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves [totalBookings, activeBookings, cancelledBookings, totalAmountSpent] for user profile.
     */
    public double[] getUserStats(int userId) throws SQLException {
        double[] stats = new double[4]; // 0: total, 1: confirmed, 2: cancelled, 3: total spent
        String sql = "SELECT " +
                "  COUNT(*) as total, " +
                "  SUM(CASE WHEN booking_status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed, " +
                "  SUM(CASE WHEN booking_status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled, " +
                "  COALESCE(SUM(CASE WHEN booking_status = 'CONFIRMED' THEN total_amount ELSE 0 END), 0) as spent " +
                "FROM bookings WHERE user_id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("total");
                    stats[1] = rs.getInt("confirmed");
                    stats[2] = rs.getInt("cancelled");
                    stats[3] = rs.getDouble("spent");
                }
            }
        }
        return stats;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("status")
        );
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}
