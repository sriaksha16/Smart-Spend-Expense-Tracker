package com.example.smartexpense.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.Notification;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.NotificationRepo;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepo notificationRepo;

    // ✅ Send email
    public void sendBudgetExceededNotification(UserReg user, String category) {
        String subject = "Budget Alert!";
        String body = "Hi " + user.getFullName() + ",\n\n"
                + "You have exceeded your budget for category: " + category;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public List<Notification> getUnreadNotifications(UserReg user) {
        return notificationRepo.findByUserAndReadFalse(user);
    }

    public void markAllAsRead(UserReg user) {
        List<Notification> unread = notificationRepo.findByUserAndReadFalse(user);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepo.saveAll(unread);
    }

    // ✅ WebSocket push ui
    public void sendWebSocketNotification(UserReg user, String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);

        System.out.println("📤 WS Notification (JSON) -> user=" + user.getEmail() + " payload=" + payload);

        try {
            String json = new ObjectMapper().writeValueAsString(payload);
            System.out.println("📤 WS payload (as JSON) -> " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Must match principal.getName() → which is email
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                payload
        );

        System.out.println("Principal name: " + user.getEmail());
        System.out.println("Sending WS payload to: " + user.getEmail());
    }

    // ✅ For expense notification
    public void notifyExpenseAdded(UserReg user, Expense expense) {
        String message = "💸 New expense added: " + expense.getTitle() + " - " + expense.getAmount();
        
        System.out.println("usemail is getting : " + user.getEmail());
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),   // ✅ must match Principal name
                "/queue/notifications",
                message
        );
    }

    // ✅ Orchestrates DB + Email + WebSocket
    public void createAndSendBudgetExceeded(UserReg user, String category) {
        // 1. Save in DB
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setCategory(category);
        notification.setMessage("⚠️ Budget exceeded for " + category);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(notification);

        // 2. Send Email
        sendBudgetExceededNotification(user, category);

        // 3. Send WebSocket
        sendWebSocketNotification(user, "⚠️ Budget exceeded for category: " + category);
    }

	public List<Notification> getUnreadNotifications(Long userid) {
		// TODO Auto-generated method stub
		 return notificationRepo.findByUser_IdAndReadFalse(userid);
	}
}