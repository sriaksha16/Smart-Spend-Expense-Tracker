package com.example.smartexpense.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartexpense.model.Budget;
import com.example.smartexpense.model.UserReg;

@Repository
public interface BudgetRepo extends JpaRepository<Budget, Long> {
	
    List<Budget> findByUserAndMonthAndYear(UserReg user, int month, int year);
    Budget findByUserAndCategoryAndMonthAndYear(UserReg user, String category, int month, int year);
    
    List<Budget> findByUser(UserReg user);
    
}