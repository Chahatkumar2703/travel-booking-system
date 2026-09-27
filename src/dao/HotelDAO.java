package dao;

import model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Hotel entities.
 */
public class HotelDAO extends BaseDAO implements GenericDAO<Hotel> {

    private static final String BASE_SELECT =
            "SELECT h.*, d.name AS destination_name, u.full_name AS agent_name " +
            "FROM hotels h " +
            "LEFT JOIN destinations d ON h.destination_id = d.id " +
            "LEFT JOIN users u ON h.agent_id = u.id ";

    @Override
    public Hotel findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE h.id = ?;";
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

    @Override
    public List<Hotel> findAll() throws SQLException {
        List<Hotel> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY h.id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Hotel> findByDestination(int destinationId) throws SQLException {
        List<Hotel> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE h.destination_id = ? AND h.status = 'ACTIVE' AND h.approval_status = 'APPROVED' ORDER BY h.price_per_night ASC;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Hotel> findApproved(String location, Double maxPrice, Double minRating, Integer destId) throws SQLException {
        List<Hotel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE h.status = 'ACTIVE' AND h.approval_status = 'APPROVED' ");
        List<Object> params = new ArrayList<>();

        if (destId != null && destId > 0) {
            sql.append("AND h.destination_id = ? ");
            params.add(destId);
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND (LOWER(h.location) LIKE ? OR LOWER(h.address) LIKE ? OR LOWER(d.name) LIKE ?) ");
            String term = "%" + location.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (maxPrice != null && maxPrice > 0) {
            sql.append("AND h.price_per_night <= ? ");
            params.add(maxPrice);
        }
        if (minRating != null && minRating > 0) {
            sql.append("AND h.rating >= ? ");
            params.add(minRating);
        }

        sql.append("ORDER BY h.rating DESC, h.price_per_night ASC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Hotel> findByAgentId(int agentId) throws SQLException {
        List<Hotel> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE h.agent_id = ? ORDER BY h.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean updateApprovalStatus(int id, String approvalStatus) throws SQLException {
        String sql = "UPDATE hotels SET approval_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, approvalStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean save(Hotel h) throws SQLException {
        String sql = "INSERT INTO hotels (agent_id, hotel_name, destination_id, address, location, room_type, " +
                "price_per_night, available_rooms, rating, description, image_url, status, approval_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getAgentId() > 0 ? h.getAgentId() : 1);
            ps.setString(2, h.getHotelName() != null ? h.getHotelName() : "");
            ps.setInt(3, h.getDestinationId() > 0 ? h.getDestinationId() : 1);
            ps.setString(4, h.getAddress() != null && !h.getAddress().isEmpty() ? h.getAddress() : (h.getLocation() != null ? h.getLocation() : "Address"));
            ps.setString(5, h.getLocation() != null && !h.getLocation().isEmpty() ? h.getLocation() : (h.getAddress() != null ? h.getAddress() : "Location"));
            ps.setString(6, h.getRoomType() != null && !h.getRoomType().isEmpty() ? h.getRoomType() : "Deluxe");
            ps.setDouble(7, h.getPricePerNight());
            ps.setInt(8, h.getAvailableRooms() > 0 ? h.getAvailableRooms() : 1);
            ps.setDouble(9, h.getRating() > 0 ? h.getRating() : 4.5);
            ps.setString(10, h.getDescription() != null ? h.getDescription() : "");
            ps.setString(11, h.getImageUrl() != null ? h.getImageUrl() : "");
            ps.setString(12, h.getStatus() != null ? h.getStatus() : "ACTIVE");
            ps.setString(13, h.getApprovalStatus() != null ? h.getApprovalStatus() : "PENDING");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        h.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Hotel h) throws SQLException {
        String sql = "UPDATE hotels SET agent_id = ?, hotel_name = ?, destination_id = ?, address = ?, location = ?, room_type = ?, " +
                "price_per_night = ?, available_rooms = ?, rating = ?, description = ?, image_url = ?, status = ?, approval_status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getAgentId() > 0 ? h.getAgentId() : 1);
            ps.setString(2, h.getHotelName());
            ps.setInt(3, h.getDestinationId());
            ps.setString(4, h.getAddress());
            ps.setString(5, h.getLocation() != null ? h.getLocation() : h.getAddress());
            ps.setString(6, h.getRoomType());
            ps.setDouble(7, h.getPricePerNight());
            ps.setInt(8, h.getAvailableRooms());
            ps.setDouble(9, h.getRating());
            ps.setString(10, h.getDescription());
            ps.setString(11, h.getImageUrl());
            ps.setString(12, h.getStatus());
            ps.setString(13, h.getApprovalStatus());
            ps.setInt(14, h.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRoomAvailability(int hotelId, int delta) throws SQLException {
        String sql = "UPDATE hotels SET available_rooms = available_rooms + ? WHERE id = ? AND available_rooms + ? >= 0;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, hotelId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM hotels WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Hotel mapRow(ResultSet rs) throws SQLException {
        Hotel h = new Hotel();
        h.setId(rs.getInt("id"));
        try { h.setAgentId(rs.getInt("agent_id")); } catch (SQLException ignored) {}
        try { h.setAgentName(rs.getString("agent_name")); } catch (SQLException ignored) {}
        h.setHotelName(rs.getString("hotel_name"));
        h.setDestinationId(rs.getInt("destination_id"));
        String destName = rs.getString("destination_name");
        h.setDestinationName(destName != null ? destName : (h.getLocation() != null ? h.getLocation() : "General"));
        h.setAddress(rs.getString("address"));
        try { h.setLocation(rs.getString("location")); } catch (SQLException ignored) {}
        h.setRoomType(rs.getString("room_type"));
        h.setPricePerNight(rs.getDouble("price_per_night"));
        h.setAvailableRooms(rs.getInt("available_rooms"));
        h.setRating(rs.getDouble("rating"));
        h.setDescription(rs.getString("description"));
        try { h.setImageUrl(rs.getString("image_url")); } catch (SQLException ignored) {}
        h.setStatus(rs.getString("status"));
        try { h.setApprovalStatus(rs.getString("approval_status")); } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            h.setCreatedAt(ts.toLocalDateTime());
        }
        return h;
    }
}
