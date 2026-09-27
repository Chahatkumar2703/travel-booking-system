package dao;

import model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Message and Feedback entities.
 */
public class MessageDAO extends BaseDAO implements GenericDAO<Message> {

    @Override
    public boolean save(Message msg) throws SQLException {
        String sql = "INSERT INTO messages (user_id, agent_id, subject, message, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, msg.getUserId());
            if (msg.getAgentId() != null && msg.getAgentId() > 0) {
                stmt.setInt(2, msg.getAgentId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, msg.getSubject());
            stmt.setString(4, msg.getMessage());
            stmt.setString(5, msg.getStatus() != null ? msg.getStatus() : "OPEN");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        msg.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Message msg) throws SQLException {
        String sql = "UPDATE messages SET subject = ?, message = ?, reply = ?, status = ?, replied_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, msg.getSubject());
            stmt.setString(2, msg.getMessage());
            stmt.setString(3, msg.getReply());
            stmt.setString(4, msg.getStatus());
            if (msg.getRepliedAt() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(msg.getRepliedAt()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            stmt.setInt(6, msg.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Message findById(int id) throws SQLException {
        String sql = "SELECT m.*, u.full_name AS user_name, u.email AS user_email, a.full_name AS agent_name " +
                     "FROM messages m " +
                     "JOIN users u ON m.user_id = u.id " +
                     "LEFT JOIN users a ON m.agent_id = a.id " +
                     "WHERE m.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMessage(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Message> findAll() throws SQLException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name AS user_name, u.email AS user_email, a.full_name AS agent_name " +
                     "FROM messages m " +
                     "JOIN users u ON m.user_id = u.id " +
                     "LEFT JOIN users a ON m.agent_id = a.id " +
                     "ORDER BY m.id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToMessage(rs));
            }
        }
        return list;
    }

    public List<Message> findByUserId(int userId) throws SQLException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name AS user_name, u.email AS user_email, a.full_name AS agent_name " +
                     "FROM messages m " +
                     "JOIN users u ON m.user_id = u.id " +
                     "LEFT JOIN users a ON m.agent_id = a.id " +
                     "WHERE m.user_id = ? " +
                     "ORDER BY m.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMessage(rs));
                }
            }
        }
        return list;
    }

    public List<Message> findByAgentId(int agentId) throws SQLException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.*, u.full_name AS user_name, u.email AS user_email, a.full_name AS agent_name " +
                     "FROM messages m " +
                     "JOIN users u ON m.user_id = u.id " +
                     "LEFT JOIN users a ON m.agent_id = a.id " +
                     "WHERE m.agent_id = ? OR m.agent_id IS NULL " +
                     "ORDER BY m.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, agentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMessage(rs));
                }
            }
        }
        return list;
    }

    public boolean reply(int id, String replyText) throws SQLException {
        String sql = "UPDATE messages SET reply = ?, status = 'REPLIED', replied_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, replyText);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setUserId(rs.getInt("user_id"));
        m.setUserName(rs.getString("user_name"));
        m.setUserEmail(rs.getString("user_email"));
        int agId = rs.getInt("agent_id");
        if (!rs.wasNull()) {
            m.setAgentId(agId);
        }
        m.setAgentName(rs.getString("agent_name"));
        m.setSubject(rs.getString("subject"));
        m.setMessage(rs.getString("message"));
        m.setReply(rs.getString("reply"));
        m.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) m.setCreatedAt(created.toLocalDateTime());
        Timestamp replied = rs.getTimestamp("replied_at");
        if (replied != null) m.setRepliedAt(replied.toLocalDateTime());
        return m;
    }
}
