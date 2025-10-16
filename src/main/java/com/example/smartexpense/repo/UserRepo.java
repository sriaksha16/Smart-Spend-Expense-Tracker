package com.example.smartexpense.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartexpense.model.UserReg;

@Repository
public interface UserRepo extends JpaRepository<UserReg, Long> {
	
	UserReg findByEmail(String email);
	  

	
	  UserReg findByfullName(String fullName);
	  
	  Optional<UserReg> findByUsername(String username);

}
