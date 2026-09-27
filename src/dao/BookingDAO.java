package dao;

import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for universal Booking entities (Packages, Flights, Hotels, Cars).
 */
public class BookingDAO extends BaseDAO implements GenericDAO<Booking> {

    private static final String BASE_SELECT =
            "SELECT b.*, u.full_name AS user_name, u.email AS user_email, " +
            "       p.package_name, d.name AS destination_name, h.hotel_name, " +
            "       f.flight_number, f.airline, c.car_name, a.full_name AS agent_name, " +
            "       pay.payment_status " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "LEFT JOIN packages p ON b.package_id = p.id " +
            "LEFT JOIN destinations d ON p.destination_id = d.id " +
            "LEFT JOIN hotels h ON b.hotel_id = h.id " +
            "LEFT JOIN flights f ON b.flight_id = f.id " +
            "LEFT JOIN cars c ON b.car_id = c.id " +
            "LEFT JOIN users a ON b.agent_id = a.id " +
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

    public List<Booking> findByAgentId(int agentId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE b.agent_id = ? OR f.agent_id = ? OR h.agent_id = ? OR c.agent_id = ? OR p.agent_id = ? " +
                     "ORDER BY b.id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            ps.setInt(2, agentId);
            ps.setInt(3, agentId);
            ps.setInt(4, agentId);
            ps.setInt(5, agentId);
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
        String sql = "INSERT INTO bookings (booking_code, user_id, booking_type, item_id, item_name, " +
                "flight_id, hotel_id, car_id, package_id, agent_id, travel_date, start_date, end_date, " +
                "persons, quantity, package_cost, hotel_cost, total_amount, special_requests, booking_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBookingCode());
            ps.setInt(2, b.getUserId());
            ps.setString(3, b.getBookingType() != null ? b.getBookingType() : "PACKAGE");

            if (b.getItemId() != null && b.getItemId() > 0) ps.setInt(4, b.getItemId());
            else ps.setNull(4, Types.INTEGER);

            ps.setString(5, b.getItemName());

            if (b.getFlightId() != null && b.getFlightId() > 0) ps.setInt(6, b.getFlightId());
            else ps.setNull(6, Types.INTEGER);

            if (b.getHotelId() != null && b.getHotelId() > 0) ps.setInt(7, b.getHotelId());
            else ps.setNull(7, Types.INTEGER);

            if (b.getCarId() != null && b.getCarId() > 0) ps.setInt(8, b.getCarId());
            else ps.setNull(8, Types.INTEGER);

            if (b.getPackageId() > 0) ps.setInt(9, b.getPackageId());
            else ps.setNull(9, Types.INTEGER);

            if (b.getAgentId() != null && b.getAgentId() > 0) ps.setInt(10, b.getAgentId());
            else ps.setNull(10, Types.INTEGER);

            ps.setDate(11, Date.valueOf(b.getTravelDate()));

            if (b.getStartDate() != null) ps.setDate(12, Date.valueOf(b.getStartDate()));
            else ps.setDate(12, Date.valueOf(b.getTravelDate()));

            if (b.getEndDate() != null) ps.setDate(13, Date.valueOf(b.getEndDate()));
            else ps.setNull(13, Types.DATE);

            ps.setInt(14, b.getPersons() > 0 ? b.getPersons() : 1);
            ps.setInt(15, b.getQuantity() > 0 ? b.getQuantity() : 1);
            ps.setDouble(16, b.getPackageCost());
            ps.setDouble(17, b.getHotelCost());
            ps.setDouble(18, b.getTotalAmount());
            ps.setString(19, b.getSpecialRequests());
            ps.setString(20, b.getBookingStatus() != null ? b.getBookingStatus() : "CONFIRMED");

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
        String sql = "UPDATE bookings SET travel_date = ?, start_date = ?, end_date = ?, persons = ?, quantity = ?, " +
                "package_cost = ?, hotel_cost = ?, total_amount = ?, special_requests = ?, booking_status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(b.getTravelDate()));
            ps.setDate(2, b.getStartDate() != null ? Date.valueOf(b.getStartDate()) : Date.valueOf(b.getTravelDate()));
            if (b.getEndDate() != null) ps.setDate(3, Date.valueOf(b.getEndDate()));
            else ps.setNull(3, Types.DATE);
            ps.setInt(4, b.getPersons());
            ps.setInt(5, b.getQuantity());
            ps.setDouble(6, b.getPackageCost());
            ps.setDouble(7, b.getHotelCost());
            ps.setDouble(8, b.getTotalAmount());
            ps.setString(9, b.getSpecialRequests());
            ps.setString(10, b.getBookingStatus());
            ps.setInt(11, b.getId());
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
        Date startD = rs.getDate("start_date");
        Date endD = rs.getDate("end_date");
        Integer hotelId = rs.getObject("hotel_id") != null ? rs.getInt("hotel_id") : null;
        Integer packageId = rs.getObject("package_id") != null ? rs.getInt("package_id") : 0;
        Integer flightId = rs.getObject("flight_id") != null ? rs.getInt("flight_id") : null;
        Integer carId = rs.getObject("car_id") != null ? rs.getInt("car_id") : null;
        Integer agentId = rs.getObject("agent_id") != null ? rs.getInt("agent_id") : null;
        Integer itemId = rs.getObject("item_id") != null ? rs.getInt("item_id") : null;
        Timestamp createdTs = rs.getTimestamp("created_at");

        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setBookingCode(rs.getString("booking_code"));
        b.setUserId(rs.getInt("user_id"));
        b.setUserName(rs.getString("user_name"));
        b.setUserEmail(rs.getString("user_email"));

        try { b.setBookingType(rs.getString("booking_type")); } catch (SQLException ignored) {}
        b.setItemId(itemId);
        try { b.setItemName(rs.getString("item_name")); } catch (SQLException ignored) {}

        b.setPackageId(packageId);
        b.setPackageName(rs.getString("package_name"));
        b.setDestinationName(rs.getString("destination_name"));
        b.setHotelId(hotelId);
        b.setHotelName(rs.getString("hotel_name"));
        b.setFlightId(flightId);
        b.setCarId(carId);
        b.setAgentId(agentId);
        try { b.setAgentName(rs.getString("agent_name")); } catch (SQLException ignored) {}

        b.setTravelDate(travelD != null ? travelD.toLocalDate() : null);
        b.setStartDate(startD != null ? startD.toLocalDate() : (travelD != null ? travelD.toLocalDate() : null));
        b.setEndDate(endD != null ? endD.toLocalDate() : null);

        b.setPersons(rs.getInt("persons"));
        try { b.setQuantity(rs.getInt("quantity")); } catch (SQLException ignored) {}
        b.setPackageCost(rs.getDouble("package_cost"));
        b.setHotelCost(rs.getDouble("hotel_cost"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setSpecialRequests(rs.getString("special_requests"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setCreatedAt(createdTs != null ? createdTs.toLocalDateTime() : null);

        try {
            String payStatus = rs.getString("payment_status");
            if (payStatus != null) {
                b.setPaymentStatus(payStatus);
            }
        } catch (SQLException ignored) {}

        return b;
    }
}
