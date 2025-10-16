package com.example.smartexpense.model;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "userbudgets")
public class Budget {
	

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
		private String category;
	    private Double amount;   // budget limit
	    public Integer getMonth() {
			return month;
		}


		public void setMonth(Integer month) {
			this.month = month;
		}


		public Integer getYear() {
			return year;
		}


		public void setYear(Integer year) {
			this.year = year;
		}



		private Integer  month;       // e.g. 9 for September
	    private Integer  year;        // e.g. 2025
	    
	    @Column(name = "created_at", columnDefinition = "DATETIME")
	    private LocalDateTime createdAt;

	    @ManyToOne
	    @JoinColumn(name = "user_id") // foreign key column in userbudgets	
	    private UserReg user;

	

	    @PrePersist
	    public void prePersist() {
	        createdAt = LocalDateTime.now();
	    }


	    public Long getId() {
			return id;
		}



		public void setId(Long id) {
			this.id = id;
		}



		public String getCategory() {
			return category;
		}



		public void setCategory(String category) {
			this.category = category;
		}



		public Double getAmount() {
			return amount;
		}



		public void setAmount(Double amount) {
			this.amount = amount;
		}



		


		public LocalDateTime getCreatedAt() {
			return createdAt;
		}



		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}



		public UserReg getUser() {
			return user;
		}



		public void setUser(UserReg user) {
			this.user = user;
		}

		@Transient
		private Double spent;

		public Double getSpent() {
		    return spent;
		}

		public void setSpent(Double spent) {
		    this.spent = spent;
		}


		    public Double getRemaining() {
		        return (amount != null ? amount : 0) - (spent != null ? spent : 0);
		    }
	
}
