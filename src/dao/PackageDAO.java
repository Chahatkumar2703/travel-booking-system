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
            "SELECT p.*, d.name AS destination_name " +
            "FROM packages p " +
            "JOIN destinations d ON p.destination_id = d.id ";

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
        String sql = BASE_SELECT + "WHERE p.destination_id = ? ORDER BY p.price_per_person ASC;";
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
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
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

    @Override
    public boolean save(TravelPackage p) throws SQLException {
        String sql = "INSERT INTO packages (package_name, destination_id, duration_days, duration_nights, " +
                "price_per_person, places_covered, hotel_included, food_included, transport_included, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPackageName());
            ps.setInt(2, p.getDestinationId());
            ps.setInt(3, p.getDurationDays());
            ps.setInt(4, p.getDurationNights());
            ps.setDouble(5, p.getPricePerPerson());
            ps.setString(6, p.getPlacesCovered());
            ps.setBoolean(7, p.isHotelIncluded());
            ps.setBoolean(8, p.isFoodIncluded());
            ps.setBoolean(9, p.isTransportIncluded());
            ps.setString(10, p.getDescription());
            ps.setString(11, p.getStatus());

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
        String sql = "UPDATE packages SET package_name = ?, destination_id = ?, duration_days = ?, duration_nights = ?, " +
                "price_per_person = ?, places_covered = ?, hotel_included = ?, food_included = ?, transport_included = ?, " +
                "description = ?, status = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPackageName());
            ps.setInt(2, p.getDestinationId());
            ps.setInt(3, p.getDurationDays());
            ps.setInt(4, p.getDurationNights());
            ps.setDouble(5, p.getPricePerPerson());
            ps.setString(6, p.getPlacesCovered());
            ps.setBoolean(7, p.isHotelIncluded());
            ps.setBoolean(8, p.isFoodIncluded());
            ps.setBoolean(9, p.isTransportIncluded());
            ps.setString(10, p.getDescription());
            ps.setString(11, p.getStatus());
            ps.setInt(12, p.getId());
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
        TravelPackage p = new TravelPackage(
                rs.getInt("id"),
                rs.getString("package_name"),
                rs.getInt("destination_id"),
                rs.getString("destination_name"),
                rs.getInt("duration_days"),
                rs.getInt("duration_nights"),
                rs.getDouble("price_per_person"),
                rs.getString("places_covered"),
                rs.getBoolean("hotel_included"),
                rs.getBoolean("food_included"),
                rs.getBoolean("transport_included"),
                rs.getString("description"),
                rs.getString("status")
        );
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
