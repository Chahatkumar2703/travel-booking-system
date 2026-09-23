package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BaseEntity provides common properties and encapsulation
 * for all persistent domain entities, demonstrating Inheritance.
 */
public abstract class BaseEntity implements Serializable {

    protected int id;
    protected LocalDateTime createdAt;

    public BaseEntity() {
    }

    public BaseEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
