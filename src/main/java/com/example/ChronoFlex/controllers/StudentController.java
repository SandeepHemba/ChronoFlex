package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/login")
    public ResponseEntity<?> activateOrLogin(@RequestBody Map<String, String> request) {

        String collegeCode = request.get("collegeCode");
        String regId = request.get("regId");
        String email = request.get("email");
        String password = request.get("password");

        if (collegeCode == null || regId == null ||
                email == null || password == null) {

            return ResponseEntity.badRequest()
                    .body("All fields are required");
        }

        try {
            String result = studentService.activateOrLogin(
                    collegeCode, regId, email, password);

            return ResponseEntity.ok(Map.of("message", result));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(e.getMessage());
        }
    }


    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {

        String collegeCode = request.get("collegeCode");
        String regId = request.get("regId");
        String email = request.get("email");
        String googleId = request.get("googleId");

        if (collegeCode == null || regId == null ||
                email == null || googleId == null) {
            return ResponseEntity.badRequest()
                    .body("All fields required");
        }

        try {
            String result = studentService
                    .googleActivateOrLogin(
                            collegeCode, regId, email, googleId);

            return ResponseEntity.ok(Map.of("message", result));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // ==============================
    // Forgot Password - Send OTP
    // ==============================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String,String> request){

        String email = request.get("email");

        if(email == null){
            return ResponseEntity.badRequest().body("Email is required");
        }

        try{
            studentService.sendForgotPasswordOtp(email);
            return ResponseEntity.ok(Map.of("message","OTP sent to email"));

        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==============================
    // Verify OTP
    // ==============================
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String,String> request){

        String email = request.get("email");
        String otp = request.get("otp");

        if(email == null || otp == null){
            return ResponseEntity.badRequest().body("Email and OTP required");
        }

        try{
            studentService.verifyOtp(email,otp);
            return ResponseEntity.ok(Map.of("message","OTP verified"));

        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==============================
    // Reset Password
    // ==============================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> request){

        String email = request.get("email");
        String password = request.get("password");

        if(email == null || password == null){
            return ResponseEntity.badRequest().body("Email and password required");
        }

        try{
            studentService.resetPassword(email,password);
            return ResponseEntity.ok(Map.of("message","Password reset successful"));

        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}