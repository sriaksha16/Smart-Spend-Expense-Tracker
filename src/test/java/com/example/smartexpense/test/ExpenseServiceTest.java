package com.example.smartexpense.test;

import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.ExpenseRepo;
import com.example.smartexpense.service.BudgetService;
import com.example.smartexpense.service.ExpenseService;
import com.example.smartexpense.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    @Mock
    private ExpenseRepo expenseRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExpenseService expenseService;

    private UserReg user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserReg();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void testSaveExpense_NewExpense() {
        Expense expense = new Expense();
        expense.setCategory("Food");
        expense.setAmount(200.0);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2025, 10, 16));

        when(expenseRepository.save(expense)).thenReturn(expense);
        when(budgetService.checkBudgetExceeded(user, "Food", 10, 2025)).thenReturn(true);

        Expense saved = expenseService.saveExpense(expense);

        assertNotNull(saved);
        verify(expenseRepository, times(1)).save(expense);
        verify(notificationService, times(1)).createAndSendBudgetExceeded(user, "Food");
    }

    @Test
    void testGetTotalIncomeAndExpenses() {
        Expense income = new Expense();
        income.setAmount(1000.0);
        income.setType("INCOME");

        Expense expense = new Expense();
        expense.setAmount(400.0);
        expense.setType("EXPENSE");

        when(expenseRepository.findByUserAndType(user, "INCOME")).thenReturn(List.of(income));
        when(expenseRepository.findByUserAndType(user, "EXPENSE")).thenReturn(List.of(expense));

        assertEquals(1000, expenseService.getTotalIncome(user));
        assertEquals(400, expenseService.getTotalExpenses(user));
        assertEquals(600, expenseService.getBalance(user));
    }

    @Test
    void testGetTransactionCount() {
        Expense e1 = new Expense();
        Expense e2 = new Expense();

        when(expenseRepository.findByUser(user)).thenReturn(List.of(e1, e2));

        assertEquals(2, expenseService.getTransactionCount(user));
    }

    @Test
    void testGetExpenseById() {
        Expense e = new Expense();
        e.setId(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(e));

        Expense fetched = expenseService.getExpenseById(1L);
        assertNotNull(fetched);
        assertEquals(1L, fetched.getId());
    }

    @Test
    void testFilterExpenses() {
        Expense e1 = new Expense();
        e1.setCategory("Food");
        e1.setAmount(100.0);
        e1.setDate(LocalDate.of(2025, 10, 10));

        Expense e2 = new Expense();
        e2.setCategory("Transport");
        e2.setAmount(200.0);
        e2.setDate(LocalDate.of(2025, 10, 15));

        when(expenseRepository.findByUser(user)).thenReturn(List.of(e1, e2));

        List<Expense> filtered = expenseService.filterExpenses(user, "Food", null, null, null, null);
        assertEquals(1, filtered.size());
        assertEquals("Food", filtered.get(0).getCategory());
    }

    @Test
    void testAddExpense_SendsNotification() {
        Expense expense = new Expense();
        expense.setCategory("Food");
        expense.setAmount(150.0);

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense saved = expenseService.addExpense(user, expense);

        assertNotNull(saved);
        verify(notificationService, times(1)).notifyExpenseAdded(user, expense);
    }

    @Test
    void testGetTotalByCategory() {
        Expense e1 = new Expense();
        e1.setAmount(100.0);
        Expense e2 = new Expense();
        e2.setAmount(200.0);

        when(expenseRepository.findByUserAndCategoryAndMonthAndYear(user, "Food", 10, 2025))
                .thenReturn(List.of(e1, e2));

        double total = expenseService.getTotalByCategory(user, "Food", 10, 2025);
        assertEquals(300, total);
    }
}
