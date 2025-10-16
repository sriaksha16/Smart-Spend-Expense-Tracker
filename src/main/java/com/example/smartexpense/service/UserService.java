package com.example.smartexpense.service;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.UserRepo;


@Service
public class UserService  implements UserDetailsService{
	
		@Autowired
		private  UserRepo userrepo;
		
	    @Autowired
	    private JavaMailSender mailSender;
	    
	    @Autowired
	    private PasswordEncoder passwordEncoder;

	
	    // Register new user with OTP
	    public UserReg registerUser(UserReg user) {
	        user.setStatus("PENDING"); // default status
	        
	        // Encode password
	        String encodedPassword = passwordEncoder.encode(user.getPassword());
	        user.setPassword(encodedPassword);
	        
	        // Generate 6-digit OTP
	        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

	        user.setOtp(otp);
	        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // expires in 5 min

	        // Save to DB
	        UserReg savedUser = userrepo.save(user);

	        // Send OTP Email
	        sendOtpEmail(savedUser.getEmail(), otp);

	        return savedUser;
	    }
	    private void sendOtpEmail(String toEmail, String otp) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(toEmail);
	        message.setSubject("SmartExpense OTP Verification");
	        message.setText("Your OTP code is: " + otp + "\nIt will expire in 5 minutes.");
	        mailSender.send(message);
	    }
	    
	    // Verify OTP
	    public boolean verifyOtp(String email, String otp) {
	        UserReg user = userrepo.findByEmail(email);
	        if (user == null) return false;

	        if (user.getOtp().equals(otp) && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
	            user.setStatus("ACTIVE");   // activate account
	            user.setOtp(null);          // clear OTP
	            user.setOtpExpiry(null);
	            userrepo.save(user);
	            return true;
	        }
	        return false;
	    }

		/*
		 * // Login method public boolean login(String email, String password) { UserReg
		 * user = userrepo.findByEmail(email); if (user != null ) { // Compare raw input
		 * with encrypted password if (passwordEncoder.matches(password,
		 * user.getPassword())) { return "ACTIVE".equals(user.getStatus()); // only
		 * active users can login
		 * 
		 * 
		 * }
		 * 
		 * System.out.println("user data" +user); } return false; }
		 */
	    
	    
	 // Send OTP for forgot password
	    public boolean sendForgotOtp(String email) {
	        UserReg user = userrepo.findByEmail(email);
	        if (user == null) return false;

	        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
	        user.setOtp(otp);
	        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
	        userrepo.save(user);

	        sendOtpEmail(email, otp);
	        return true;
	    }
	    
	    
	 // Reset password after OTP verification
	    public boolean resetPassword(String email, String otp, String newPassword) {
	        UserReg user = userrepo.findByEmail(email);
	        if (user == null) return false;

	        if (user.getOtp().equals(otp) && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
	            user.setPassword(newPassword);
	            user.setOtp(null);
	            user.setOtpExpiry(null);
	            userrepo.save(user);
	            return true;
	        }
	        return false;
	    }	    
	    
	    

	    @Override
	    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	        UserReg user = userrepo.findByEmail(email);
	        		 if (user == null) {
	        		        throw new UsernameNotFoundException("User not found with email: " + email);
	        		    }
	        		 
	        		 System.out.println("🔑 Password from DB = " + user.getPassword());
	        		 System.out.println("📝 Password matches? " +
	        		     passwordEncoder.matches("11", user.getPassword()));
	        
	        return org.springframework.security.core.userdetails.User
	                .withUsername(user.getEmail())
	                .password(user.getPassword()) 
	                .authorities("USER")
	                .build();
	    }
		public UserReg findByEmail(String email) {
			// TODO Auto-generated method stub
			return userrepo.findByEmail(email);
		}
		
		public UserReg findByfullName(String name) {
			// TODO Auto-generated method stub
			return userrepo.findByfullName(name);
		}
		


	}
	    
















