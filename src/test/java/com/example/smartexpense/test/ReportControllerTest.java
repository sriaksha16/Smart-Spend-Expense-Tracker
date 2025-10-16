package com.example.smartexpense.test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

import com.example.smartexpense.service.ReportService;
import com.example.smartexpense.service.UserService;
import com.example.smartexpense.controller.ReportController;
import com.example.smartexpense.model.UserReg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;
    private Principal mockPrincipal;
    private UserReg mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();

        // Simulate logged-in user
        mockPrincipal = () -> "test@example.com";
        mockUser = new UserReg();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
    }

    @Test
    void testGetPieChartData_ReturnsCategoryBreakdown() throws Exception {
        Map<String, Double> breakdown = Map.of("Food", 500.0, "Transport", 200.0);

        when(reportService.getCategoryBreakdown(1L, 2025, 10)).thenReturn(breakdown);

        mockMvc.perform(get("/reports/pie/1/2025/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Food").value(500.0))
                .andExpect(jsonPath("$.Transport").value(200.0));
    }

    @Test
    void testViewReports_ReturnsReportsView() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);

        mockMvc.perform(get("/reports/viewreports").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attributeExists("year"))
                .andExpect(model().attributeExists("month"));
    }
}
