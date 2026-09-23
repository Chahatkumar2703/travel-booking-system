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
            "SELECT h.*, d.name AS destination_name " +
            "FROM hotels h " +
            "JOIN destinations d ON h.destination_id = d.id ";

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
        String sql = BASE_SELECT + "WHERE h.destination_id = ? ORDER BY h.price_per_night ASC;";
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

    @Override
    public boolean save(Hotel h) throws SQLException {
        String sql = "INSERT INTO hotels (hotel_name, destination_id, address, room_type, " +
                "price_per_night, available_rooms, rating, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getHotelName());
            ps.setInt(2, h.getDestinationId());
            ps.setString(3, h.getAddress());
            ps.setString(4, h.getRoomType());
            ps.setDouble(5, h.getPricePerNight());
            ps.setInt(6, h.getAvailableRooms());
            ps.setDouble(7, h.getRating());
            ps.setString(8, h.getDescription());
            ps.setString(9, h.getStatus());

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
        String sql = "UPDATE hotels SET hotel_name = ?, destination_id = ?, address = ?, room_type = ?, " +
                "price_per_night = ?, available_rooms = ?, rating = ?, description = ?, status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getHotelName());
            ps.setInt(2, h.getDestinationId());
            ps.setString(3, h.getAddress());
            ps.setString(4, h.getRoomType());
            ps.setDouble(5, h.getPricePerNight());
            ps.setInt(6, h.getAvailableRooms());
            ps.setDouble(7, h.getRating());
            ps.setString(8, h.getDescription());
            ps.setString(9, h.getStatus());
            ps.setInt(10, h.getId());
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
        Hotel h = new Hotel(
                rs.getInt("id"),
                rs.getString("hotel_name"),
                rs.getInt("destination_id"),
                rs.getString("destination_name"),
                rs.getString("address"),
                rs.getString("room_type"),
                rs.getDouble("price_per_night"),
                rs.getInt("available_rooms"),
                rs.getDouble("rating"),
                rs.getString("description"),
                rs.getString("status")
        );
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            h.setCreatedAt(ts.toLocalDateTime());
        }
        return h;
    }
}
