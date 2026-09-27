package dao;

import model.TravelPackage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for TravelPackage entities.
 */
public class PackageDAO extends BaseDAO implements GenericDAO<TravelPackage> {

    private static final String BASE_SELECT =
            "SELECT p.*, d.name AS destination_name, u.full_name AS agent_name " +
            "FROM packages p " +
            "JOIN destinations d ON p.destination_id = d.id " +
            "LEFT JOIN users u ON p.agent_id = u.id ";

    @Override
    public TravelPackage findById(int id) throws SQLException {
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

    @Override
    public List<TravelPackage> findAll() throws SQLException {
        List<TravelPackage> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY p.id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<TravelPackage> findByDestination(int destinationId) throws SQLException {
        List<TravelPackage> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.destination_id = ? AND p.status = 'ACTIVE' AND p.approval_status = 'APPROVED' ORDER BY p.price_per_person ASC;";
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

    public List<TravelPackage> search(String keyword, Integer destinationId, Double maxPrice) throws SQLException {
        List<TravelPackage> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE p.status = 'ACTIVE' AND p.approval_status = 'APPROVED' ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.package_name LIKE ? OR p.places_covered LIKE ? OR d.name LIKE ?) ");
            String wildcard = "%" + keyword.trim() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        if (destinationId != null && destinationId > 0) {
            sql.append("AND p.destination_id = ? ");
            params.add(destinationId);
        }

        if (maxPrice != null && maxPrice > 0) {
            sql.append("AND p.price_per_person <= ? ");
            params.add(maxPrice);
        }

        sql.append("ORDER BY p.price_per_person ASC;");

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

    public List<TravelPackage> findByAgentId(int agentId) throws SQLException {
        List<TravelPackage> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.agent_id = ? ORDER BY p.id DESC";
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
        String sql = "UPDATE packages SET approval_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, approvalStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean save(TravelPackage p) throws SQLException {
        String sql = "INSERT INTO packages (agent_id, package_name, destination_id, duration_days, duration_nights, " +
                "price_per_person, places_covered, hotel_included, food_included, transport_included, description, image_url, status, approval_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getAgentId() > 0 ? p.getAgentId() : 1);
            ps.setString(2, p.getPackageName());
            ps.setInt(3, p.getDestinationId());
            ps.setInt(4, p.getDurationDays());
            ps.setInt(5, p.getDurationNights());
            ps.setDouble(6, p.getPricePerPerson());
            ps.setString(7, p.getPlacesCovered());
            ps.setBoolean(8, p.isHotelIncluded());
            ps.setBoolean(9, p.isFoodIncluded());
            ps.setBoolean(10, p.isTransportIncluded());
            ps.setString(11, p.getDescription());
            ps.setString(12, p.getImageUrl() != null ? p.getImageUrl() : "");
            ps.setString(13, p.getStatus() != null ? p.getStatus() : "ACTIVE");
            ps.setString(14, p.getApprovalStatus() != null ? p.getApprovalStatus() : "APPROVED");

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
    public boolean update(TravelPackage p) throws SQLException {
        String sql = "UPDATE packages SET agent_id = ?, package_name = ?, destination_id = ?, duration_days = ?, duration_nights = ?, " +
                "price_per_person = ?, places_covered = ?, hotel_included = ?, food_included = ?, transport_included = ?, " +
                "description = ?, image_url = ?, status = ?, approval_status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getAgentId() > 0 ? p.getAgentId() : 1);
            ps.setString(2, p.getPackageName());
            ps.setInt(3, p.getDestinationId());
            ps.setInt(4, p.getDurationDays());
            ps.setInt(5, p.getDurationNights());
            ps.setDouble(6, p.getPricePerPerson());
            ps.setString(7, p.getPlacesCovered());
            ps.setBoolean(8, p.isHotelIncluded());
            ps.setBoolean(9, p.isFoodIncluded());
            ps.setBoolean(10, p.isTransportIncluded());
            ps.setString(11, p.getDescription());
            ps.setString(12, p.getImageUrl());
            ps.setString(13, p.getStatus());
            ps.setString(14, p.getApprovalStatus());
            ps.setInt(15, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM packages WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private TravelPackage mapRow(ResultSet rs) throws SQLException {
        TravelPackage p = new TravelPackage();
        p.setId(rs.getInt("id"));
        try { p.setAgentId(rs.getInt("agent_id")); } catch (SQLException ignored) {}
        try { p.setAgentName(rs.getString("agent_name")); } catch (SQLException ignored) {}
        p.setPackageName(rs.getString("package_name"));
        p.setDestinationId(rs.getInt("destination_id"));
        p.setDestinationName(rs.getString("destination_name"));
        p.setDurationDays(rs.getInt("duration_days"));
        p.setDurationNights(rs.getInt("duration_nights"));
        p.setPricePerPerson(rs.getDouble("price_per_person"));
        p.setPlacesCovered(rs.getString("places_covered"));
        p.setHotelIncluded(rs.getBoolean("hotel_included"));
        p.setFoodIncluded(rs.getBoolean("food_included"));
        p.setTransportIncluded(rs.getBoolean("transport_included"));
        p.setDescription(rs.getString("description"));
        try { p.setImageUrl(rs.getString("image_url")); } catch (SQLException ignored) {}
        p.setStatus(rs.getString("status"));
        try { p.setApprovalStatus(rs.getString("approval_status")); } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
