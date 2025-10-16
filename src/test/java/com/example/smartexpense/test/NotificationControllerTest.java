package com.example.smartexpense.test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.List;

import com.example.smartexpense.controller.NotificationController;
import com.example.smartexpense.model.Notification;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.service.NotificationService;
import com.example.smartexpense.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private Principal mockPrincipal;
    private UserReg mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        mockPrincipal = () -> "user@example.com"; // Mock logged-in user
        mockUser = new UserReg();
        mockUser.setEmail("user@example.com");
    }

    @Test
    void testViewNotifications_ReturnsNotificationsPage() throws Exception {
        Notification n1 = new Notification();
        n1.setMessage("Budget limit exceeded!");
        Notification n2 = new Notification();
        n2.setMessage("New expense added!");

        when(userService.findByEmail("user@example.com")).thenReturn(mockUser);
        when(notificationService.getUnreadNotifications(mockUser))
                .thenReturn(List.of(n1, n2));

        mockMvc.perform(get("/notifications").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"));
    }

    @Test
    void testMarkAllAsRead_RedirectsToNotifications() throws Exception {
        when(userService.findByEmail("user@example.com")).thenReturn(mockUser);

        mockMvc.perform(post("/notifications/mark-read").principal(mockPrincipal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService, times(1)).markAllAsRead(mockUser);
    }
}
