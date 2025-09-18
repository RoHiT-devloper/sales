// src/main/java/com/salesSavvy/controller/ForgotPasswordController.java
package com.salesSavvy.controller;

import com.salesSavvy.entity.Users;
import com.salesSavvy.repository.UsersRepository;
import com.salesSavvy.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@CrossOrigin("*")
public class ForgotPasswordController {
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private EmailService emailService;
    
    // In-memory storage for OTPs (in production, use Redis or database)
    private Map<String, String> otpStorage = new HashMap<>();
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        Users user = usersRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("Email not found");
        }
        
        // Generate OTP
        String otp = generateOtp();
        
        // Store OTP temporarily
        otpStorage.put(email, otp);
        
        // Send OTP via email
        try {
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send OTP");
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            // OTP is valid
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        
        // Verify OTP first
        String storedOtp = otpStorage.get(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
        
        Users user = usersRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        // Update password
        user.setPassword(newPassword); // In production, hash the password
        usersRepository.save(user);
        
        // Remove OTP from storage
        otpStorage.remove(email);
        
        return ResponseEntity.ok("Password reset successfully");
    }
    
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}