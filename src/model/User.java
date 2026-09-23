package model;

/**
 * User entity representing system users and administrators.
 */
public class User extends BaseEntity {

    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String role; // "USER" or "ADMIN"
    private String status; // "ACTIVE" or "DISABLED"

    public User() {
        super();
        this.role = "USER";
        this.status = "ACTIVE";
    }

    public User(int id, String fullName, String email, String phone, String passwordHash, String role, String status) {
        super(id);
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : "USER";
        this.status = status != null ? status : "ACTIVE";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}
