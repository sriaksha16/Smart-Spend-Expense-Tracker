package com.example.smartexpense.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "userreg")
public class UserReg {

	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // Primary key
    
    private String fullName;
    private String email;
	private String username;
    private String password;
    private String status;
	private String otp;
	@Column(name = "otp_expiry", columnDefinition = "DATETIME")
    private LocalDateTime otpExpiry;
    
    
    
	
    public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public LocalDateTime getOtpExpiry() {
		return otpExpiry;
	}
	public void setOtpExpiry(LocalDateTime otpExpiry) {
		this.otpExpiry = otpExpiry;
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
	
	   public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}

	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	
	   public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		
	
}
