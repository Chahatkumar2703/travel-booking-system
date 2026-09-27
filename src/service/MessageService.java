package service;

import dao.MessageDAO;
import model.Message;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing messages and feedback between travelers, agents, and admins.
 */
public class MessageService {

    private final MessageDAO messageDAO;

    public MessageService() {
        this.messageDAO = new MessageDAO();
    }

    public Message sendMessage(int userId, Integer agentId, String subject, String messageText)
            throws ValidationException, DatabaseException {

        if (subject == null || subject.trim().isEmpty()) {
            throw new ValidationException("Subject is required.");
        }
        if (messageText == null || messageText.trim().isEmpty()) {
            throw new ValidationException("Message content cannot be empty.");
        }

        Message msg = new Message();
        msg.setUserId(userId);
        msg.setAgentId(agentId != null && agentId > 0 ? agentId : null);
        msg.setSubject(subject.trim());
        msg.setMessage(messageText.trim());
        msg.setStatus("OPEN");

        try {
            messageDAO.save(msg);
            return msg;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to send message: " + e.getMessage(), e);
        }
    }

    public List<Message> getUserMessages(int userId) throws DatabaseException {
        try {
            return messageDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve user messages: " + e.getMessage(), e);
        }
    }

    public List<Message> getAgentMessages(int agentId) throws DatabaseException {
        try {
            return messageDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent messages: " + e.getMessage(), e);
        }
    }

    public List<Message> getAllMessages() throws DatabaseException {
        try {
            return messageDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all messages: " + e.getMessage(), e);
        }
    }

    public boolean replyMessage(int messageId, String replyText) throws ValidationException, DatabaseException {
        if (replyText == null || replyText.trim().isEmpty()) {
            throw new ValidationException("Reply cannot be empty.");
        }
        try {
            return messageDAO.reply(messageId, replyText.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to submit reply: " + e.getMessage(), e);
        }
    }

    public boolean deleteMessage(int messageId) throws DatabaseException {
        try {
            return messageDAO.delete(messageId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete message: " + e.getMessage(), e);
        }
    }
}
