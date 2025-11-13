package com.example.ChronoFlex.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.service.CollegeService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/colleges")
@CrossOrigin(origins = "*")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @PostMapping("/register")
    public ResponseEntity<String> initiateRegistration(@RequestBody College college) {
        try {
            collegeService.initiateRegistration(college);
            return ResponseEntity.ok("OTP sent. Verify to complete registration.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String collegeCode = request.get("collegeCode");
        String otp = request.get("otp");

        if (collegeCode == null || otp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("collegeCode and otp are required");
        }

        boolean success = collegeService.verifyOtpAndRegister(collegeCode, otp);
        if (success) {
            return ResponseEntity.ok("OTP verified. College registered successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
    }



    @GetMapping
    public List<College> getAllColleges() {
        return collegeService.getAllColleges();
    }

    @GetMapping("/{collegeCode}")
    public College getCollegeByCode(@PathVariable String collegeCode) {
        return collegeService.getCollegeByCode(collegeCode);
    }
}
