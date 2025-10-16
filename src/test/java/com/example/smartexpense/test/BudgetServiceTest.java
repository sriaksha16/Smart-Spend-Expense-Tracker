package com.example.smartexpense.test;

import com.example.smartexpense.model.Budget;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.BudgetRepo;
import com.example.smartexpense.repo.ExpenseRepo;
import com.example.smartexpense.service.BudgetService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetServiceTest {

    @Mock
    private BudgetRepo budgetRepository;

    @Mock
    private ExpenseRepo expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

    private UserReg user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserReg();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void testSaveBudget() {
        Budget budget = new Budget();
        budget.setCategory("Food");
        budget.setAmount(50.0);
        budget.setUser(user);

        when(budgetRepository.save(budget)).thenReturn(budget);

        Budget saved = budgetService.saveBudget(budget);
        assertNotNull(saved);
        assertEquals("Food", saved.getCategory());
        verify(budgetRepository, times(1)).save(budget);
    }

    @Test
    void testGetBudgetsForMonth() {
        Budget budget = new Budget();
        budget.setCategory("Food");
        budget.setAmount(500.0);
        budget.setUser(user);
        budget.setMonth(10);
        budget.setYear(2025);

        Expense expense = new Expense();
        expense.setAmount(200.0);

        when(budgetRepository.findByUserAndMonthAndYear(user, 10, 2025))
                .thenReturn(List.of(budget));
        when(expenseRepository.getTotalSpent(user.getId(), "Food", 10, 2025))
                .thenReturn(200.0);

        List<Budget> budgets = budgetService.getBudgetsForMonth(user, 10, 2025);

        assertEquals(1, budgets.size());
        assertEquals(200.0, budgets.get(0).getSpent());
    }

    @Test
    void testCheckBudgetExceeded_True() {
        Budget budget = new Budget();
        budget.setAmount(500.0);
        budget.setCategory("Food");
        budget.setUser(user);
        budget.setMonth(10);
        budget.setYear(2025);

        Expense expense1 = new Expense();
        expense1.setAmount(300.0);
        Expense expense2 = new Expense();
        expense2.setAmount(250.0);

        when(budgetRepository.findByUserAndCategoryAndMonthAndYear(user, "Food", 10, 2025))
                .thenReturn(budget);
        when(expenseRepository.findByUserAndCategoryAndMonthAndYear(user, "Food", 10, 2025))
                .thenReturn(List.of(expense1, expense2));

        boolean exceeded = budgetService.checkBudgetExceeded(user, "Food", 10, 2025);
        assertTrue(exceeded);
    }

    @Test
    void testGetRemaining() {
        Budget budget = new Budget();
        budget.setAmount(500.0);
        budget.setCategory("Food");
        budget.setUser(user);
        budget.setMonth(10);
        budget.setYear(2025);

        Expense expense1 = new Expense();
        expense1.setAmount(200.0);

        when(budgetRepository.findByUserAndCategoryAndMonthAndYear(user, "Food", 10, 2025))
                .thenReturn(budget);
        when(expenseRepository.findByUserAndCategoryAndMonthAndYear(user, "Food", 10, 2025))
                .thenReturn(List.of(expense1));

        double remaining = budgetService.getRemaining(user, "Food", 10, 2025);
        assertEquals(300.0, remaining);
    }

    @Test
    void testGetMonthName() {
        assertEquals("January", BudgetService.getMonthName(1));
        assertEquals("December", BudgetService.getMonthName(12));
        assertEquals("Invalid Month", BudgetService.getMonthName(13));
    }

    @Test
    void testGetSpentForCategory() {
        Expense expense1 = new Expense();
        expense1.setAmount(100.0);
        Expense expense2 = new Expense();
        expense2.setAmount(200.0);

        when(expenseRepository.findByUserAndCategoryAndMonthAndYear(user, "Food", 10, 2025))
                .thenReturn(List.of(expense1, expense2));

        double spent = budgetService.getSpentForCategory(user, "Food", 10, 2025);
        assertEquals(300.0, spent);
    }
}
