package com.example.smartexpense.test;

import com.example.smartexpense.controller.BudgetController;
import com.example.smartexpense.model.Budget;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.BudgetRepo;
import com.example.smartexpense.service.BudgetService;
import com.example.smartexpense.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.http.MediaType;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private UserService userService;

    @Mock
    private BudgetRepo budgetRepo;

    @InjectMocks
    private BudgetController budgetController;

    private MockMvc mockMvc;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();

        mockPrincipal = () -> "test@example.com"; // mock authenticated user
    }

    // ✅ Test GET /budgets/new
    @Test
    void testShowBudgetForm() throws Exception {
        mockMvc.perform(get("/budgets/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("budget_form"))
                .andExpect(model().attributeExists("budget"))
                .andExpect(model().attributeExists("categories"));
    }

    // ✅ Test POST /budgets/save
    @Test
    void testSaveBudget() throws Exception {
        UserReg user = new UserReg();
        user.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(post("/budgets/save")
                        .principal(mockPrincipal)
                        .param("category", "Food")
                        .param("amount", "1000")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budgets/view"));

        verify(budgetService, times(1)).saveBudget(any(Budget.class));
    }

    // ✅ Test GET /budgets/view
    @Test
    void testViewBudgets() throws Exception {
        UserReg user = new UserReg();
        user.setEmail("test@example.com");

        Budget b1 = new Budget();
        b1.setCategory("Food");
        b1.setAmount(1000.0);

        Budget b2 = new Budget();
        b2.setCategory("Transport");
        b2.setAmount(500.0);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetService.getBudgetsForMonth(eq(user), anyInt(), anyInt()))
                .thenReturn(List.of(b1, b2));
        when(budgetService.getSpentForCategory(any(), anyString(), anyInt(), anyInt()))
                .thenReturn(200.0);

        mockMvc.perform(get("/budgets/view").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("budget_list"))
                .andExpect(model().attributeExists("budgets"))
                .andExpect(model().attributeExists("totalBudget"))
                .andExpect(model().attributeExists("totalSpent"))
                .andExpect(model().attributeExists("remaining"))
                .andExpect(model().attributeExists("totalCategories"));

        verify(budgetService, times(1)).getBudgetsForMonth(eq(user), anyInt(), anyInt());
    }

    // ✅ Test GET /budgets/budgets
    @Test
    void testViewRemainings() throws Exception {
        UserReg user = new UserReg();
        user.setEmail("test@example.com");

        Budget b1 = new Budget();
        b1.setCategory("Food");
        b1.setAmount(1000.0);
        b1.setMonth(LocalDate.now().getMonthValue());
        b1.setYear(LocalDate.now().getYear());

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepo.findByUser(user)).thenReturn(List.of(b1));
        when(budgetService.getSpentForCategory(eq(user), anyString(), anyInt(), anyInt()))
                .thenReturn(400.0);

        mockMvc.perform(get("/budgets/budgets").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("budget_list"))
                .andExpect(model().attributeExists("budgets"))
                .andExpect(model().attributeExists("totalBudget"))
                .andExpect(model().attributeExists("remaining"))
                .andExpect(model().attributeExists("totalCategories"));

        verify(budgetRepo, times(1)).findByUser(user);
        verify(budgetService, times(1)).getSpentForCategory(eq(user), anyString(), anyInt(), anyInt());
    }
}
