package com.example.smartexpense.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.smartexpense.model.Notification;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.service.NotificationService;
import com.example.smartexpense.service.UserService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;


    // 📌 Show unread notifications in UI
    @GetMapping
    public String viewNotifications(Model model, Principal principal) {
        System.out.println("Principal username = " + principal.getName());
        
        UserReg user = userService.findByEmail(principal.getName());
        
        System.out.println("Sending WebSocket to = " + user.getEmail());
        
        List<Notification> unread = notificationService.getUnreadNotifications(user);
        
        model.addAttribute("notifications", unread);
        
        return "notifications"; // → thymeleaf page to show them
    }

		    // 📌 Mark all notifications as read
		    @PostMapping("/mark-read")
		    public String markAllAsRead(Principal principal) {
		        UserReg user = userService.findByEmail(principal.getName());
		        notificationService.markAllAsRead(user);
		        return "redirect:/notifications";
		    }
		    

		
		
}
