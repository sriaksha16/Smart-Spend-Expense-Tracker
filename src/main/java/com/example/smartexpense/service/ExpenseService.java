package com.example.smartexpense.service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.ExpenseRepo;



@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepo expenseRepository;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private NotificationService notificationService;
    
    

    public List<Expense> getExpensesByUser(UserReg user) {
        return expenseRepository.findByUser(user);
    }

    
    public Expense saveExpense(Expense expense) {
        if (expense.getId() != null) {
            // Existing expense -> update
            Expense existing = expenseRepository.findById(expense.getId())
                .orElseThrow(() -> new RuntimeException("Expense not found"));

            // Preserve the original createdAt
            expense.setCreatedAt(existing.getCreatedAt());
        }
        // updatedAt will be handled by @PreUpdate

        Expense saved = expenseRepository.save(expense);

        // ---- Budget Check after saving ----
        int month = saved.getDate().getMonthValue();
        int year = saved.getDate().getYear();

        boolean exceeded = budgetService.checkBudgetExceeded(saved.getUser(), saved.getCategory(), month, year);

        if (exceeded) {
           
            // Delegate everything to NotificationService
            notificationService.createAndSendBudgetExceeded(saved.getUser(), saved.getCategory());
        
        }

        return saved;
    }

    
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
    
    public double getTotalIncome(UserReg user) {
        return expenseRepository.findByUserAndType(user, "INCOME")
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getTotalExpenses(UserReg user) {
        return expenseRepository.findByUserAndType(user, "EXPENSE")
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double getBalance(UserReg user) {
        return getTotalIncome(user) - getTotalExpenses(user);
    }

    public long getTransactionCount(UserReg user) {
        return expenseRepository.findByUser(user).size();
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }
	
	public List<Expense> getAllExpenses(UserReg user) {
	    return expenseRepository.findByUser(user);
	}

	public double getTotalByCategory(UserReg user, String category, int month, int year) {
	    return expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
	            .stream()
	            .mapToDouble(Expense::getAmount)
	            .sum();
	}
	
	  public Expense addExpense(UserReg user, Expense expense) {
	        Expense savedExpense = expenseRepository.save(expense);

	        // 🔔 Send WebSocket notification
	        notificationService.notifyExpenseAdded(user, savedExpense);

	        return savedExpense;
	    }

	  // Flexible filter (date, category, amount)
	  public List<Expense> filterExpenses(UserReg user, String category, LocalDate start, LocalDate end, Double min, Double max) {
		    List<Expense> all = expenseRepository.findByUser(user);

		    return all.stream()
		            .filter(e -> category == null || category.isEmpty() || e.getCategory().equalsIgnoreCase(category))
		            .filter(e -> start == null || !e.getDate().isBefore(start))
		            .filter(e -> end == null || !e.getDate().isAfter(end))
		            .filter(e -> min == null || e.getAmount() >= min)
		            .filter(e -> max == null || e.getAmount() <= max)
		            .collect(Collectors.toList());
		}

	

	   
}
