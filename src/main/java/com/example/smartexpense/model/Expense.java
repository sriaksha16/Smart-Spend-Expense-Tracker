package com.example.smartexpense.model;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;



@Entity
@Table(name="userexpenses")
public class Expense {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	 

		private String title;
	    private Double amount;
	    private String category;
	    private String type; // EXPENSE / INCOME
	    private LocalDate date;
	    private String description;
	    
	    @Column(name = "created_at", columnDefinition = "DATETIME")
	    private LocalDateTime createdAt;  // when first created
	    
	    @Column(name = "updated_at", columnDefinition = "DATETIME")
	    private LocalDateTime updatedAt;  // when last updated
	    
	    
	    @PrePersist
	    public void prePersist() {
	        createdAt = LocalDateTime.now();
	        updatedAt = LocalDateTime.now();
	    }

	    @PreUpdate
	    public void preUpdate() {
	        updatedAt = LocalDateTime.now();
	    }
	    
	    public LocalDateTime getCreatedAt() {
	        return createdAt;
	    }

	    public void setCreatedAt(LocalDateTime createdAt) {
	        this.createdAt = createdAt;
	    }

	    public LocalDateTime getUpdatedAt() {
	        return updatedAt;
	    }

	    public void setUpdatedAt(LocalDateTime updatedAt) {
	        this.updatedAt = updatedAt;
	    }

	    
	    public Long getId() {
		return id;
	}

	 public void setId(Long id) {
		 this.id = id;
	 }

	 public String getTitle() {
		 return title;
	 }

	 public void setTitle(String title) {
		 this.title = title;
	 }

	 public Double getAmount() {
		 return amount;
	 }

	 public void setAmount(Double amount) {
		 this.amount = amount;
	 }

	 public String getCategory() {
		 return category;
	 }

	 public void setCategory(String category) {
		 this.category = category;
	 }

	 public String getType() {
		 return type;
	 }

	 public void setType(String type) {
		 this.type = type;
	 }

	 public LocalDate getDate() {
		 return date;
	 }

	 public void setDate(LocalDate date) {
		 this.date = date;
	 }

	 public String getDescription() {
		 return description;
	 }

	 public void setDescription(String description) {
		 this.description = description;
	 }

	 public UserReg getUser() {
		 return user;
	 }

	 public void setUser(UserReg user) {
		 this.user = user;
	 }

	    @ManyToOne
	    @JoinColumn(name = "user_id")
	    private UserReg user;  // link to logged-in user

}
