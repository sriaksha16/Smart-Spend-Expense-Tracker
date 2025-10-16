package com.example.smartexpense.test;

import com.example.smartexpense.model.Expense;
import com.example.smartexpense.repo.ExpenseRepo;
import com.example.smartexpense.service.ReportService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private ExpenseRepo expenseRepo;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        expenseRepo = mock(ExpenseRepo.class);
        reportService = new ReportService(expenseRepo);
    }

    @Test
    void testGetCategoryBreakdown() {
        Object[] row1 = new Object[]{"Food", "Lunch", "EXPENSE", 150.0};
        Object[] row2 = new Object[]{"Transport", "Bus", "EXPENSE", 50.0};

        when(expenseRepo.getCategoryTotalsDetailed(1L, 2025, 10))
                .thenReturn(Arrays.asList(row1, row2));

        Map<String, Double> breakdown = reportService.getCategoryBreakdown(1L, 2025, 10);

        assertEquals(2, breakdown.size());
        assertEquals(150.0, breakdown.get("Food - Lunch (EXPENSE)"));
        assertEquals(50.0, breakdown.get("Transport - Bus (EXPENSE)"));
    }

    @Test
    void testGeneratePdf() {
        Expense e1 = new Expense();
        e1.setTitle("Lunch");
        e1.setAmount(150.0);
        e1.setCategory("Food");
        e1.setType("EXPENSE");
        e1.setDate(LocalDate.now());
        e1.setDescription("Lunch with friends");

        List<Expense> expenses = Arrays.asList(e1);

        ByteArrayInputStream pdfStream = reportService.generatePdf(expenses, "John Doe");

        assertNotNull(pdfStream);
        assertTrue(pdfStream.available() > 0);
    }

    @Test
    void testGenerateExcel() {
        Expense e1 = new Expense();
        e1.setTitle("Dinner");
        e1.setAmount(200.0);
        e1.setCategory("Food");
        e1.setType("EXPENSE");
        e1.setDate(LocalDate.now());
        e1.setDescription("Dinner with family");

        List<Expense> expenses = Arrays.asList(e1);

        ByteArrayInputStream excelStream = reportService.generateExcel(expenses, "John Doe");

        assertNotNull(excelStream);
        assertTrue(excelStream.available() > 0);
    }
}
