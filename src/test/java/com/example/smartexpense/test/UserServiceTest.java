package com.example.smartexpense.test;

import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.UserRepo;
import com.example.smartexpense.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepo userRepo;
    private JavaMailSender mailSender;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        mailSender = mock(JavaMailSender.class);
        passwordEncoder = mock(PasswordEncoder.class);

   
    }

    @Test
    void testRegisterUser() {
        UserReg user = new UserReg();
        user.setEmail("test@example.com");
        user.setPassword("1234");
        user.setFullName("John Doe");

        when(passwordEncoder.encode("1234")).thenReturn("encoded1234");
        when(userRepo.save(any(UserReg.class))).thenAnswer(i -> i.getArguments()[0]);

        UserReg saved = userService.registerUser(user);

        assertEquals("encoded1234", saved.getPassword());
        assertNotNull(saved.getOtp());
        assertEquals("PENDING", saved.getStatus());
        assertTrue(saved.getOtpExpiry().isAfter(LocalDateTime.now()));

        // Verify email sent
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testVerifyOtpSuccess() {
        UserReg user = new UserReg();
        user.setEmail("test@example.com");
        user.setOtp("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setStatus("PENDING");

        when(userRepo.findByEmail("test@example.com")).thenReturn(user);
        when(userRepo.save(any(UserReg.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean result = userService.verifyOtp("test@example.com", "123456");

        assertTrue(result);
        assertEquals("ACTIVE", user.getStatus());
        assertNull(user.getOtp());
        assertNull(user.getOtpExpiry());
    }

    @Test
    void testVerifyOtpFailure() {
        UserReg user = new UserReg();
        user.setEmail("test@example.com");
        user.setOtp("123456");
        user.setOtpExpiry(LocalDateTime.now().minusMinutes(1)); // expired

        when(userRepo.findByEmail("test@example.com")).thenReturn(user);

        boolean result = userService.verifyOtp("test@example.com", "123456");
        assertFalse(result);
    }

    @Test
    void testSendForgotOtp() {
        UserReg user = new UserReg();
        user.setEmail("forgot@example.com");

        when(userRepo.findByEmail("forgot@example.com")).thenReturn(user);
        when(userRepo.save(any(UserReg.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean sent = userService.sendForgotOtp("forgot@example.com");
        assertTrue(sent);
        assertNotNull(user.getOtp());
        assertTrue(user.getOtpExpiry().isAfter(LocalDateTime.now()));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testResetPasswordSuccess() {
        UserReg user = new UserReg();
        user.setEmail("reset@example.com");
        user.setOtp("111111");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepo.findByEmail("reset@example.com")).thenReturn(user);
        when(userRepo.save(any(UserReg.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean reset = userService.resetPassword("reset@example.com", "111111", "newpass");
        assertTrue(reset);
        assertEquals("newpass", user.getPassword());
        assertNull(user.getOtp());
        assertNull(user.getOtpExpiry());
    }

    @Test
    void testResetPasswordFailure() {
        UserReg user = new UserReg();
        user.setEmail("reset@example.com");
        user.setOtp("111111");
        user.setOtpExpiry(LocalDateTime.now().minusMinutes(1)); // expired

        when(userRepo.findByEmail("reset@example.com")).thenReturn(user);

        boolean reset = userService.resetPassword("reset@example.com", "111111", "newpass");
        assertFalse(reset);
    }
}
