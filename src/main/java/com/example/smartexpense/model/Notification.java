package com.example.smartexpense.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserReg user;

    private String message;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    // 👇 add this if you want to track category
    private String category;
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    @Column(name = "is_read", columnDefinition = "TINYINT(1)")
    private boolean read;  // false = unread, true = read

    // Constructors
    public Notification() {}

    public Notification(UserReg user, String message) {
        this.user = user;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public Notification(UserReg user, String message, String category) {
        this.user = user;
        this.message = message;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserReg getUser() { return user; }
    public void setUser(UserReg user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}