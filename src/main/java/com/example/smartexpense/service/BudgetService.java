/*package com.example.smartexpense.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.smartexpense.model.Budget;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.BudgetRepo;
import com.example.smartexpense.repo.ExpenseRepo;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepo budgetRepository;



    @Autowired
    private ExpenseRepo expenseRepository; // use repo directly

    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsForMonth(UserReg user, int month, int year) {
        return budgetRepository.findByUserAndMonthAndYear(user, month, year);
        
        
    }

    public boolean checkBudgetExceeded(UserReg user, String category, int month, int year) {
        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year);
        if (budget == null) return false;

		
		 * double spent = expenseService.getTotalByCategory(user, category, month,
		 * year); return spent >= budget.getAmount();
		 
        
        double spent = expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        return spent >= budget.getAmount();

    }

    public double getRemaining(UserReg user, String category, int month, int year) {
        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year);
        if (budget == null) return 0;

        double spent = expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        return budget.getAmount() - spent;
    }
    
    // 👇 Added here
    public static String getMonthName(int month) {
        String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        if (month < 1 || month > 12) {
            return "Invalid Month";
        }
        return MONTH_NAMES[month - 1];
    }

	public double getSpentForCategory(UserReg user, String category, int month, int year) {
		  return expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
	                .stream()
	                .mapToDouble(Expense::getAmount)
	                .sum();
	}
	
}
*/

package com.example.smartexpense.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.smartexpense.model.Budget;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.BudgetRepo;
import com.example.smartexpense.repo.ExpenseRepo;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepo budgetRepository;

    @Autowired
    private ExpenseRepo expenseRepository;

    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsForMonth(UserReg user, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);

        // enrich each budget with spent amount
        for (Budget b : budgets) {
            Double spent = expenseRepository.getTotalSpent(
                b.getUser().getId(),
                b.getCategory(),
                b.getMonth(),
                b.getYear()
            );
            b.setSpent(spent);
        }

        return budgets;
    }

    public boolean checkBudgetExceeded(UserReg user, String category, int month, int year) {
        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year);
        if (budget == null) return false;

        double spent = expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        return spent >= budget.getAmount();
    }

    public double getRemaining(UserReg user, String category, int month, int year) {
        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year);
        if (budget == null) return 0;

        double spent = expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        return budget.getAmount() - spent;
    }

    public static String getMonthName(int month) {
        String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        if (month < 1 || month > 12) {
            return "Invalid Month";
        }
        return MONTH_NAMES[month - 1];
    }

    public double getSpentForCategory(UserReg user, String category, int month, int year) {
        return expenseRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}

