package dao;

import model.Destination;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Destination entities.
 */
public class DestinationDAO extends BaseDAO implements GenericDAO<Destination> {

    @Override
    public Destination findById(int id) throws SQLException {
        String sql = "SELECT * FROM destinations WHERE id = ?;";
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

    public Destination findByName(String name) throws SQLException {
        String sql = "SELECT * FROM destinations WHERE LOWER(name) = LOWER(?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Destination> findAll() throws SQLException {
        List<Destination> list = new ArrayList<>();
        String sql = "SELECT * FROM destinations ORDER BY name ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Destination> search(String keyword) throws SQLException {
        List<Destination> list = new ArrayList<>();
        String sql = "SELECT * FROM destinations WHERE name LIKE ? OR state LIKE ? OR attractions LIKE ? ORDER BY name ASC;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + keyword.trim() + "%";
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
    public boolean save(Destination d) throws SQLException {
        String sql = "INSERT INTO destinations (name, state, description, attractions, best_time) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getState());
            ps.setString(3, d.getDescription());
            ps.setString(4, d.getAttractions());
            ps.setString(5, d.getBestTime());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        d.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Destination d) throws SQLException {
        String sql = "UPDATE destinations SET name = ?, state = ?, description = ?, attractions = ?, best_time = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getState());
            ps.setString(3, d.getDescription());
            ps.setString(4, d.getAttractions());
            ps.setString(5, d.getBestTime());
            ps.setInt(6, d.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM destinations WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Destination mapRow(ResultSet rs) throws SQLException {
        Destination d = new Destination(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("state"),
                rs.getString("description"),
                rs.getString("attractions"),
                rs.getString("best_time")
        );
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            d.setCreatedAt(ts.toLocalDateTime());
        }
        return d;
    }
}
