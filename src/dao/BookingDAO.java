package dao;

import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Booking entities.
 */
public class BookingDAO extends BaseDAO implements GenericDAO<Booking> {

    private static final String BASE_SELECT =
            "SELECT b.*, u.full_name AS user_name, u.email AS user_email, " +
            "       p.package_name, d.name AS destination_name, h.hotel_name, " +
            "       pay.payment_status " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN packages p ON b.package_id = p.id " +
            "JOIN destinations d ON p.destination_id = d.id " +
            "LEFT JOIN hotels h ON b.hotel_id = h.id " +
            "LEFT JOIN payments pay ON b.id = pay.booking_id ";

    @Override
    public Booking findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.id = ?;";
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

    public Booking findByBookingCode(String code) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.booking_code = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> findByUserId(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE b.user_id = ? ORDER BY b.id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY b.id DESC;";
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
    public boolean save(Booking b) throws SQLException {
        String sql = "INSERT INTO bookings (booking_code, user_id, package_id, hotel_id, travel_date, " +
                "persons, package_cost, hotel_cost, total_amount, special_requests, booking_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBookingCode());
            ps.setInt(2, b.getUserId());
            ps.setInt(3, b.getPackageId());
            if (b.getHotelId() != null && b.getHotelId() > 0) {
                ps.setInt(4, b.getHotelId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setDate(5, Date.valueOf(b.getTravelDate()));
            ps.setInt(6, b.getPersons());
            ps.setDouble(7, b.getPackageCost());
            ps.setDouble(8, b.getHotelCost());
            ps.setDouble(9, b.getTotalAmount());
            ps.setString(10, b.getSpecialRequests());
            ps.setString(11, b.getBookingStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        b.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Booking b) throws SQLException {
        String sql = "UPDATE bookings SET travel_date = ?, persons = ?, package_cost = ?, hotel_cost = ?, " +
                "total_amount = ?, special_requests = ?, booking_status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(b.getTravelDate()));
            ps.setInt(2, b.getPersons());
            ps.setDouble(3, b.getPackageCost());
            ps.setDouble(4, b.getHotelCost());
            ps.setDouble(5, b.getTotalAmount());
            ps.setString(6, b.getSpecialRequests());
            ps.setString(7, b.getBookingStatus());
            ps.setInt(8, b.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        return updateStatus(bookingId, "CANCELLED");
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Date travelD = rs.getDate("travel_date");
        Integer hotelId = rs.getObject("hotel_id") != null ? rs.getInt("hotel_id") : null;
        Timestamp createdTs = rs.getTimestamp("created_at");

        Booking b = new Booking(
                rs.getInt("id"),
                rs.getString("booking_code"),
                rs.getInt("user_id"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getInt("package_id"),
                rs.getString("package_name"),
                rs.getString("destination_name"),
                hotelId,
                rs.getString("hotel_name"),
                travelD != null ? travelD.toLocalDate() : null,
                rs.getInt("persons"),
                rs.getDouble("package_cost"),
                rs.getDouble("hotel_cost"),
                rs.getDouble("total_amount"),
                rs.getString("special_requests"),
                rs.getString("booking_status"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );

        try {
            String payStatus = rs.getString("payment_status");
            if (payStatus != null) {
                b.setPaymentStatus(payStatus);
            }
        } catch (SQLException ignored) {
        }

        return b;
    }
}
