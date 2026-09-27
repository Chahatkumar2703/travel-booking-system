package model;

import java.time.LocalDateTime;

/**
 * Message / Feedback entity representing communication between travelers, agents, and admins.
 */
public class Message extends BaseEntity {

    private int userId;
    private String userName;   // Populated via JOIN
    private String userEmail;  // Populated via JOIN
    private Integer agentId;   // Nullable (null = admin/general inquiry)
    private String agentName;  // Populated via JOIN
    private String subject;
    private String message;
    private String reply;
    private String status;     // "OPEN", "REPLIED", "CLOSED"
    private LocalDateTime repliedAt;

    public Message() {
        super();
        this.status = "OPEN";
    }

    public Message(int id, int userId, String userName, String userEmail,
                   Integer agentId, String agentName, String subject,
                   String message, String reply, String status,
                   LocalDateTime createdAt, LocalDateTime repliedAt) {
        super(id);
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.agentId = agentId;
        this.agentName = agentName;
        this.subject = subject;
        this.message = message;
        this.reply = reply;
        this.status = status != null ? status : "OPEN";
        this.createdAt = createdAt;
        this.repliedAt = repliedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }
}
