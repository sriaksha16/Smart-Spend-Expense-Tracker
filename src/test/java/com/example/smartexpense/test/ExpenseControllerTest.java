package com.example.smartexpense.test;

import com.example.smartexpense.controller.ExpenseController;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.service.ExpenseService;
import com.example.smartexpense.service.ReportService;
import com.example.smartexpense.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserService userService;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ExpenseController expenseController;

    private MockMvc mockMvc;
    private Principal mockPrincipal;
    private UserReg mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();

        mockPrincipal = () -> "test@example.com";
        mockUser = new UserReg();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("John Doe");
    }

    // ✅ 1. Test - GET /expenses/expenseshome
    @Test
    void testListExpenses() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        when(expenseService.getAllExpenses(mockUser)).thenReturn(List.of(new Expense()));

        mockMvc.perform(get("/expenses/expenseshome").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses"))
                .andExpect(model().attributeExists("expenses"))
                .andExpect(model().attributeExists("currentuser"))
                .andExpect(model().attributeExists("user"));

        verify(expenseService).getAllExpenses(mockUser);
    }

    // ✅ 2. Test - GET /expenses/add/new
    @Test
    void testShowExpenseForm() throws Exception {
        mockMvc.perform(get("/expenses/add/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenseeditform"))
                .andExpect(model().attributeExists("expense"));
    }

    // ✅ 3. Test - POST /expenses/save
    @Test
    void testSaveExpense() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);

        mockMvc.perform(post("/expenses/save")
                        .principal(mockPrincipal)
                        .param("description", "Groceries")
                        .param("amount", "500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/expenseshome"));

        verify(expenseService).saveExpense(any(Expense.class));
    }

    // ✅ 4. Test - GET /expenses/edit/{id}
    @Test
    void testEditExpense() throws Exception {
        Expense expense = new Expense();
        expense.setId(10L);
        expense.setDescription("Food");

        when(expenseService.getExpenseById(10L)).thenReturn(expense);

        mockMvc.perform(get("/expenses/edit/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenseeditform"))
                .andExpect(model().attributeExists("expense"));
    }

    // ✅ 5. Test - GET /expenses/delete/{id}
    @Test
    void testDeleteExpense() throws Exception {
        mockMvc.perform(get("/expenses/delete/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/expenseshome"));

        verify(expenseService).deleteExpense(5L);
    }

    // ✅ 6. Test - GET /expenses/reports
    @Test
    void testShowReports() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);

        mockMvc.perform(get("/expenses/reports").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("categoryTotals"))
                .andExpect(model().attributeExists("months"))
                .andExpect(model().attributeExists("monthlyExpenses"))
                .andExpect(model().attributeExists("totalIncome"))
                .andExpect(model().attributeExists("totalExpenses"));
    }

    // ✅ 7. Test - GET /expenses/export/pdf
    @Test
    void testExportPdf() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        when(expenseService.getAllExpenses(mockUser)).thenReturn(List.of(new Expense()));
        when(reportService.generatePdf(anyList(), anyString()))
                .thenReturn(new ByteArrayInputStream("PDF data".getBytes()));

        mockMvc.perform(get("/expenses/export/pdf").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=expenses_John Doe.pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    // ✅ 8. Test - GET /expenses/export/excel
    @Test
    void testExportExcel() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        when(expenseService.getAllExpenses(mockUser)).thenReturn(List.of(new Expense()));
        when(reportService.generateExcel(anyList(), anyString()))
                .thenReturn(new ByteArrayInputStream("Excel data".getBytes()));

        mockMvc.perform(get("/expenses/export/excel").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=expenses_John Doe.xlsx"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    // ✅ 9. Test - GET /expenses/filter
    @Test
    void testFilterExpenses() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        when(expenseService.filterExpenses(eq(mockUser), anyString(), any(), any(), any(), any()))
                .thenReturn(List.of(new Expense()));

        mockMvc.perform(get("/expenses/filter")
                        .principal(mockPrincipal)
                        .param("category", "Food")
                        .param("min", "100")
                        .param("max", "500"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses"))
                .andExpect(model().attributeExists("expenses"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("currentuser"));
    }
}
