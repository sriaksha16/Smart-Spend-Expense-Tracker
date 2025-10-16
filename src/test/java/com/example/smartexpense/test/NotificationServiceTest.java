package com.example.smartexpense.test;

import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.Notification;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.NotificationRepo;
import com.example.smartexpense.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private NotificationService notificationService;

    private UserReg user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserReg();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
    }

    @Test
    void testSendBudgetExceededNotification() {
        notificationService.sendBudgetExceededNotification(user, "Food");

        // Verify that email is sent
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendWebSocketNotification() {
        notificationService.sendWebSocketNotification(user, "Test Message");

        // Verify WebSocket message sent
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq(user.getEmail()), eq("/queue/notifications"), any());
    }

    @Test
    void testNotifyExpenseAdded() {
        Expense expense = new Expense();
        expense.setTitle("Lunch");
        expense.setAmount(200.0);

        notificationService.notifyExpenseAdded(user, expense);

        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq(user.getEmail()), eq("/queue/notifications"), any());
    }

    @Test
    void testCreateAndSendBudgetExceeded() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setCategory("Food");
        notification.setMessage("⚠️ Budget exceeded for Food");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepo.save(any(Notification.class))).thenReturn(notification);

        notificationService.createAndSendBudgetExceeded(user, "Food");

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq(user.getEmail()), eq("/queue/notifications"), any());
    }

    @Test
    void testGetUnreadNotificationsByUser() {
        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(notificationRepo.findByUserAndReadFalse(user)).thenReturn(List.of(n1, n2));

        List<Notification> unread = notificationService.getUnreadNotifications(user);

        assertEquals(2, unread.size());
        verify(notificationRepo, times(1)).findByUserAndReadFalse(user);
    }

    @Test
    void testMarkAllAsRead() {
        Notification n1 = new Notification();
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setRead(false);

        when(notificationRepo.findByUserAndReadFalse(user)).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(user);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepo, times(1)).saveAll(anyList());
    }
}
