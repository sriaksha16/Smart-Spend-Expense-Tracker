package com.example.smartexpense.test;

import com.example.smartexpense.controller.UserController;
import com.example.smartexpense.model.Notification;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.service.NotificationService;
import com.example.smartexpense.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testUserHome_WithAuthenticatedUser() throws Exception {
        UserReg user = new UserReg();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        // create notification using no-args + setters
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setMessage("New expense added");
        notif.setCategory("expense");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);

        when(notificationService.getUnreadNotifications(user)).thenReturn(List.of(notif));

        mockMvc.perform(get("/userhome").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("userhome"))
                .andExpect(model().attributeExists("currentUserEmail"))
                .andExpect(model().attributeExists("notifications"));
    }

    @Test
    void testGetNotificationsApi() throws Exception {
        UserReg user = new UserReg();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        Notification notif = new Notification();
        notif.setId(1L);
        notif.setMessage("New expense added");
        notif.setCategory("expense");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);

        when(notificationService.getUnreadNotifications(user)).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/notifications").principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.notifications[0].message").value("New expense added"));
    }

    @Test
    void testClearNotificationsApi() throws Exception {
        UserReg user = new UserReg();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        doNothing().when(notificationService).markAllAsRead(user);

        mockMvc.perform(post("/api/notifications/clear").principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All notifications cleared"));
    }
}
