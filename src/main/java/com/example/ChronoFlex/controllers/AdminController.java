package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> initiateAdminRegistration(@RequestBody Admin admin) {
        try {
            adminService.initiateAdminRegistration(admin);
            return ResponseEntity.ok("OTP sent to college for verification.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP.");
        }
    }

    @PostMapping("/verify-college-otp")
    public ResponseEntity<String> verifyCollegeOtp(@RequestBody Map<String, String> request) {
        String collegeCode = request.get("collegeCode");
        String otp = request.get("otp");

        try {
            boolean success = adminService.verifyCollegeOtp(collegeCode, otp);
            if (success)
                return ResponseEntity.ok("College OTP verified. OTP sent to admin email.");
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP/Invalid College Code.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/verify-admin-otp")
    public ResponseEntity<String> verifyAdminOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email and OTP are required.");

        boolean success = adminService.verifyAdminOtp(email, otp);
        if (success)
            return ResponseEntity.ok("Admin registered successfully!");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP/Invalid Admin Email ID.");
    }

    // --- NEW LOGIN endpoint ---
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email and password are required");
        }

        try {
            Admin admin = adminService.loginAdmin(email, password);
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "adminName", admin.getName(),
                    "collegeCode", admin.getCollegeCode()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        try {
            adminService.sendPasswordResetOtp(email);
            return ResponseEntity.ok("OTP sent to your registered email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Email, OTP, or new password are required.");
        }

        try {
            adminService.resetPassword(email, otp, newPassword);
            return ResponseEntity.ok("Password reset successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Password reset failed.");
        }
    }

    // 🔹 Fetch college code by admin credentials
    @PostMapping("/fetch-college-code")
    public ResponseEntity<?> getCollegeCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            String collegeCode = adminService.getCollegeCodeByCredentials(email, password);
            return ResponseEntity.ok(Map.of("collegeCode", collegeCode));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
