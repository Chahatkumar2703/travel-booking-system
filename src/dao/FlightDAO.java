package dao;

import model.Flight;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Flight entities.
 */
public class FlightDAO extends BaseDAO implements GenericDAO<Flight> {

    @Override
    public boolean save(Flight flight) throws SQLException {
        String sql = "INSERT INTO flights (agent_id, airline, flight_number, origin, destination, " +
                     "departure_date, departure_time, arrival_time, price, available_seats, image_url, status, approval_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, flight.getAgentId());
            stmt.setString(2, flight.getAirline());
            stmt.setString(3, flight.getFlightNumber());
            stmt.setString(4, flight.getOrigin());
            stmt.setString(5, flight.getDestination());
            stmt.setDate(6, Date.valueOf(flight.getDepartureDate()));
            stmt.setString(7, flight.getDepartureTime());
            stmt.setString(8, flight.getArrivalTime());
            stmt.setDouble(9, flight.getPrice());
            stmt.setInt(10, flight.getAvailableSeats());
            stmt.setString(11, flight.getImageUrl() != null ? flight.getImageUrl() : "");
            stmt.setString(12, flight.getStatus() != null ? flight.getStatus() : "ACTIVE");
            stmt.setString(13, flight.getApprovalStatus() != null ? flight.getApprovalStatus() : "PENDING");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        flight.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Flight flight) throws SQLException {
        String sql = "UPDATE flights SET agent_id = ?, airline = ?, flight_number = ?, origin = ?, " +
                     "destination = ?, departure_date = ?, departure_time = ?, arrival_time = ?, " +
                     "price = ?, available_seats = ?, image_url = ?, status = ?, approval_status = ? " +
                     "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, flight.getAgentId());
            stmt.setString(2, flight.getAirline());
            stmt.setString(3, flight.getFlightNumber());
            stmt.setString(4, flight.getOrigin());
            stmt.setString(5, flight.getDestination());
            stmt.setDate(6, Date.valueOf(flight.getDepartureDate()));
            stmt.setString(7, flight.getDepartureTime());
            stmt.setString(8, flight.getArrivalTime());
            stmt.setDouble(9, flight.getPrice());
            stmt.setInt(10, flight.getAvailableSeats());
            stmt.setString(11, flight.getImageUrl());
            stmt.setString(12, flight.getStatus());
            stmt.setString(13, flight.getApprovalStatus());
            stmt.setInt(14, flight.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM flights WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Flight findById(int id) throws SQLException {
        String sql = "SELECT f.*, u.full_name AS agent_name FROM flights f " +
                     "LEFT JOIN users u ON f.agent_id = u.id WHERE f.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFlight(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Flight> findAll() throws SQLException {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name AS agent_name FROM flights f " +
                     "LEFT JOIN users u ON f.agent_id = u.id ORDER BY f.departure_date ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToFlight(rs));
            }
        }
        return list;
    }

    public List<Flight> findApproved(String origin, String destination, String dateStr, Double maxPrice, String airline) throws SQLException {
        List<Flight> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT f.*, u.full_name AS agent_name FROM flights f " +
                                              "LEFT JOIN users u ON f.agent_id = u.id " +
                                              "WHERE f.status = 'ACTIVE' AND f.approval_status = 'APPROVED' ");
        List<Object> params = new ArrayList<>();

        if (origin != null && !origin.trim().isEmpty()) {
            sql.append("AND LOWER(f.origin) LIKE ? ");
            params.add("%" + origin.trim().toLowerCase() + "%");
        }
        if (destination != null && !destination.trim().isEmpty()) {
            sql.append("AND LOWER(f.destination) LIKE ? ");
            params.add("%" + destination.trim().toLowerCase() + "%");
        }
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                sql.append("AND f.departure_date >= ? ");
                params.add(Date.valueOf(LocalDate.parse(dateStr.trim())));
            } catch (Exception ignored) {}
        }
        if (maxPrice != null && maxPrice > 0) {
            sql.append("AND f.price <= ? ");
            params.add(maxPrice);
        }
        if (airline != null && !airline.trim().isEmpty()) {
            sql.append("AND LOWER(f.airline) LIKE ? ");
            params.add("%" + airline.trim().toLowerCase() + "%");
        }

        sql.append("ORDER BY f.departure_date ASC, f.price ASC");

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFlight(rs));
                }
            }
        }
        return list;
    }

    public List<Flight> findByAgentId(int agentId) throws SQLException {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name AS agent_name FROM flights f " +
                     "LEFT JOIN users u ON f.agent_id = u.id WHERE f.agent_id = ? ORDER BY f.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, agentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFlight(rs));
                }
            }
        }
        return list;
    }

    public boolean updateApprovalStatus(int id, String approvalStatus) throws SQLException {
        String sql = "UPDATE flights SET approval_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, approvalStatus);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean decrementSeats(int flightId, int seats) throws SQLException {
        String sql = "UPDATE flights SET available_seats = available_seats - ? WHERE id = ? AND available_seats >= ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seats);
            stmt.setInt(2, flightId);
            stmt.setInt(3, seats);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean incrementSeats(int flightId, int seats) throws SQLException {
        String sql = "UPDATE flights SET available_seats = available_seats + ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seats);
            stmt.setInt(2, flightId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Flight mapResultSetToFlight(ResultSet rs) throws SQLException {
        Flight f = new Flight();
        f.setId(rs.getInt("id"));
        f.setAgentId(rs.getInt("agent_id"));
        try {
            f.setAgentName(rs.getString("agent_name"));
        } catch (SQLException ignored) {}
        f.setAirline(rs.getString("airline"));
        f.setFlightNumber(rs.getString("flight_number"));
        f.setOrigin(rs.getString("origin"));
        f.setDestination(rs.getString("destination"));
        Date depDate = rs.getDate("departure_date");
        if (depDate != null) {
            f.setDepartureDate(depDate.toLocalDate());
        }
        f.setDepartureTime(rs.getString("departure_time"));
        f.setArrivalTime(rs.getString("arrival_time"));
        f.setPrice(rs.getDouble("price"));
        f.setAvailableSeats(rs.getInt("available_seats"));
        f.setImageUrl(rs.getString("image_url"));
        f.setStatus(rs.getString("status"));
        f.setApprovalStatus(rs.getString("approval_status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            f.setCreatedAt(ts.toLocalDateTime());
        }
        return f;
    }
}
