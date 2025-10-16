package com.example.smartexpense.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.model.Notification;
import com.example.smartexpense.service.UserService;
import com.example.smartexpense.service.NotificationService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private UserService userservice;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("user/login")
    public String userlogin() {
        return "userlogin";
    }

    @GetMapping("/register")
    public String userreg() {
        return "userregister";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/user/login"; 
    }

    @PostMapping("/userregister")
    public String registerUser(@ModelAttribute UserReg user, Model model) {
        userservice.registerUser(user);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("message", "OTP sent to your email. Please verify.");
        return "otpverify"; 
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        boolean verified = userservice.verifyOtp(email, otp);
        if (verified) {
            model.addAttribute("message", "Account verified! Please login.");
            return "userlogin";
        } else {
            model.addAttribute("error", "Invalid or expired OTP.");
            return "otpverify";
        }
    }

    @GetMapping("/userhome")
    public String userHome(Model model, HttpServletResponse response, Authentication authentication) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (authentication == null) {
            return "redirect:/user/login";
        }

        String email = authentication.getName(); 
        
        System.out.println("✅ Logged-in user: " + email);

        // Get current user
        UserReg currentUser = userservice.findByEmail(email);
        
        Long userid= currentUser.getId();       
        
        // Get unread notifications for the current user
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userid);
        
        model.addAttribute("currentUserEmail", email);
        model.addAttribute("notifications", unreadNotifications);

        return "userhome";
    }

    // API to get notifications as JSON (for AJAX calls)
    @GetMapping("/api/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return response;
        }

        String email = authentication.getName();
        UserReg currentUser = userservice.findByEmail(email);
        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser);

        // Convert to frontend format
        List<Map<String, String>> notificationList = notifications.stream().map(notification -> {
            Map<String, String> notifMap = new HashMap<>();
            notifMap.put("id", notification.getId().toString());
            notifMap.put("message", notification.getMessage());
            notifMap.put("time", formatTimeAgo(notification.getCreatedAt()));
            notifMap.put("icon", getIconForCategory(notification.getCategory()));
            return notifMap;
        }).collect(Collectors.toList());

        response.put("success", true);
        response.put("notifications", notificationList);
        response.put("count", notifications.size());
        
        return response;
    }

    // API to mark all notifications as read
    @PostMapping("/api/notifications/clear")
    @ResponseBody
    public Map<String, Object> clearNotifications(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return response;
        }

        String email = authentication.getName();
        UserReg currentUser = userservice.findByEmail(email);
        
        try {
            notificationService.markAllAsRead(currentUser);
            response.put("success", true);
            response.put("message", "All notifications cleared");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing notifications");
        }
        
        return response;
    }

    @GetMapping("/forgot/password")
    public String forgotpsw() {
        return "forgotpassword";
    }

    @PostMapping("/forgot/sendOtp")
    public String sendForgotOtp(@RequestParam String email, Model model) {
        boolean userExists = userservice.sendForgotOtp(email);
        if (userExists) {
            model.addAttribute("email", email);
            model.addAttribute("message", "OTP sent to your email. Enter it below.");
            return "resetpassword";  
        } else {
            model.addAttribute("error", "Email not registered.");
            return "forgotpassword";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String otp,
                                @RequestParam String newPassword,
                                Model model) {
        boolean reset = userservice.resetPassword(email, otp, newPassword);
        if (reset) {
            model.addAttribute("message", "Password reset successful! Please login.");
            return "userlogin";
        } else {
            model.addAttribute("error", "Invalid OTP or expired.");
            model.addAttribute("email", email);
            return "resetpassword";
        }
    }

    // Helper method to format time ago
    private String formatTimeAgo(java.time.LocalDateTime createdAt) {
        java.time.Duration duration = java.time.Duration.between(createdAt, java.time.LocalDateTime.now());
        
        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + " minutes ago";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + " hours ago";
        } else {
            return duration.toDays() + " days ago";
        }
    }

    // Helper method to get appropriate icon based on category
    private String getIconForCategory(String category) {
        if (category == null) {
            return "fas fa-bell";
        }
        
        switch (category.toLowerCase()) {
            case "budget":
                return "fas fa-chart-pie";
            case "expense":
                return "fas fa-exclamation-triangle";
            case "bill":
                return "fas fa-file-invoice-dollar";
            default:
                return "fas fa-bell";
        }
    }
}