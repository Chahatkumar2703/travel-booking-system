package dao;

import model.Car;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Rental Car entities.
 */
public class CarDAO extends BaseDAO implements GenericDAO<Car> {

    @Override
    public boolean save(Car car) throws SQLException {
        String sql = "INSERT INTO cars (agent_id, car_name, brand, model, location, car_type, " +
                     "price_per_day, available_units, image_url, status, approval_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, car.getAgentId());
            stmt.setString(2, car.getCarName());
            stmt.setString(3, car.getBrand());
            stmt.setString(4, car.getModel());
            stmt.setString(5, car.getLocation());
            stmt.setString(6, car.getCarType() != null ? car.getCarType() : "Sedan");
            stmt.setDouble(7, car.getPricePerDay());
            stmt.setInt(8, car.getAvailableUnits());
            stmt.setString(9, car.getImageUrl() != null ? car.getImageUrl() : "");
            stmt.setString(10, car.getStatus() != null ? car.getStatus() : "ACTIVE");
            stmt.setString(11, car.getApprovalStatus() != null ? car.getApprovalStatus() : "PENDING");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        car.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Car car) throws SQLException {
        String sql = "UPDATE cars SET agent_id = ?, car_name = ?, brand = ?, model = ?, location = ?, " +
                     "car_type = ?, price_per_day = ?, available_units = ?, image_url = ?, status = ?, approval_status = ? " +
                     "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, car.getAgentId());
            stmt.setString(2, car.getCarName());
            stmt.setString(3, car.getBrand());
            stmt.setString(4, car.getModel());
            stmt.setString(5, car.getLocation());
            stmt.setString(6, car.getCarType());
            stmt.setDouble(7, car.getPricePerDay());
            stmt.setInt(8, car.getAvailableUnits());
            stmt.setString(9, car.getImageUrl());
            stmt.setString(10, car.getStatus());
            stmt.setString(11, car.getApprovalStatus());
            stmt.setInt(12, car.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Car findById(int id) throws SQLException {
        String sql = "SELECT c.*, u.full_name AS agent_name FROM cars c " +
                     "LEFT JOIN users u ON c.agent_id = u.id WHERE c.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCar(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Car> findAll() throws SQLException {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS agent_name FROM cars c " +
                     "LEFT JOIN users u ON c.agent_id = u.id ORDER BY c.id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCar(rs));
            }
        }
        return list;
    }

    public List<Car> findApproved(String location, String carType, Double maxPrice) throws SQLException {
        List<Car> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT c.*, u.full_name AS agent_name FROM cars c " +
                                              "LEFT JOIN users u ON c.agent_id = u.id " +
                                              "WHERE c.status = 'ACTIVE' AND c.approval_status = 'APPROVED' ");
        List<Object> params = new ArrayList<>();

        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND LOWER(c.location) LIKE ? ");
            params.add("%" + location.trim().toLowerCase() + "%");
        }
        if (carType != null && !carType.trim().isEmpty() && !"ALL".equalsIgnoreCase(carType.trim())) {
            sql.append("AND LOWER(c.car_type) = ? ");
            params.add(carType.trim().toLowerCase());
        }
        if (maxPrice != null && maxPrice > 0) {
            sql.append("AND c.price_per_day <= ? ");
            params.add(maxPrice);
        }

        sql.append("ORDER BY c.price_per_day ASC");

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCar(rs));
                }
            }
        }
        return list;
    }

    public List<Car> findByAgentId(int agentId) throws SQLException {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS agent_name FROM cars c " +
                     "LEFT JOIN users u ON c.agent_id = u.id WHERE c.agent_id = ? ORDER BY c.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, agentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCar(rs));
                }
            }
        }
        return list;
    }

    public boolean updateApprovalStatus(int id, String approvalStatus) throws SQLException {
        String sql = "UPDATE cars SET approval_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, approvalStatus);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean decrementUnits(int carId, int units) throws SQLException {
        String sql = "UPDATE cars SET available_units = available_units - ? WHERE id = ? AND available_units >= ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, units);
            stmt.setInt(2, carId);
            stmt.setInt(3, units);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean incrementUnits(int carId, int units) throws SQLException {
        String sql = "UPDATE cars SET available_units = available_units + ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, units);
            stmt.setInt(2, carId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Car mapResultSetToCar(ResultSet rs) throws SQLException {
        Car c = new Car();
        c.setId(rs.getInt("id"));
        c.setAgentId(rs.getInt("agent_id"));
        try {
            c.setAgentName(rs.getString("agent_name"));
        } catch (SQLException ignored) {}
        c.setCarName(rs.getString("car_name"));
        c.setBrand(rs.getString("brand"));
        c.setModel(rs.getString("model"));
        c.setLocation(rs.getString("location"));
        c.setCarType(rs.getString("car_type"));
        c.setPricePerDay(rs.getDouble("price_per_day"));
        c.setAvailableUnits(rs.getInt("available_units"));
        c.setImageUrl(rs.getString("image_url"));
        c.setStatus(rs.getString("status"));
        c.setApprovalStatus(rs.getString("approval_status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }
}
