package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.FacultyResponseDTO;
import com.example.ChronoFlex.model.Faculty;
import com.example.ChronoFlex.service.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
@CrossOrigin(origins = "*")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @PostMapping("/register")
    public ResponseEntity<?> registerFaculty(@RequestBody Map<String, Object> request) {
        try {
            Faculty faculty = facultyService.registerFacultyByAdmin(
                    (String) request.get("adminEmail"),
                    (String) request.get("adminPassword"),
                    (String) request.get("facultyName"),
                    (String) request.get("facultyEmail"),
                    (String) request.get("department"),
                    (String) request.get("collegeCode"),
                    (Integer) request.get("maxHoursPerDay"),
                    (Integer) request.get("maxHoursPerWeek"),
                    (String) request.get("dob"),
                    (String) request.get("qualification")
            );

            FacultyResponseDTO responseDTO = new FacultyResponseDTO(
                    faculty.getFacultyId(),
                    faculty.getName(),
                    faculty.getEmail(),
                    faculty.getDepartment(),
                    faculty.getCollege().getCollegeCode(),
                    faculty.getCreatedBy().getName(),
                    faculty.getMaxHoursPerDay(),
                    faculty.getMaxHoursPerWeek(),
                    faculty.getDob(),
                    faculty.getQualification(),
                    faculty.getActive()
            );



            return ResponseEntity.ok(Map.of(
                    "message", "Faculty registered successfully",
                    "faculty", responseDTO,
                    "defaultPassword", "Sent via email"
            ));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // ============================================================
    // 1️⃣ Send Password Reset OTP
    // ============================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendPasswordResetOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
        }

        try {
            facultyService.sendPasswordResetOtp(email);
            return ResponseEntity.ok("Password reset OTP sent to your email.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send OTP.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // ============================================================
    // 2️⃣ Reset Password
    // ============================================================
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email, OTP, and new password are required");
        }

        try {
            facultyService.resetPassword(email, otp, newPassword);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    // ============================================================
    // 🔹 Fetch All Faculties by Admin Credentials (with Filters)
    // ============================================================
    @GetMapping("/fetch-by-admin")
    public ResponseEntity<?> getFacultiesByAdmin(
            @RequestParam String adminEmail,
            @RequestParam String password,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) Boolean active
    ) {
        try {
            List<FacultyResponseDTO> facultyList = facultyService.getFacultiesByAdmin(
                    adminEmail,
                    password,
                    department,
                    qualification,
                    active
            );

            if (facultyList.isEmpty()) {
                return ResponseEntity.ok("No faculties found for this admin with the given filters.");
            }

            return ResponseEntity.ok(facultyList);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ============================================================
    // 🔹 Faculty Login
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<?> loginFaculty(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        try {
            Faculty faculty = facultyService.loginFaculty(email, password);

            // Map to DTO
            FacultyResponseDTO responseDTO = new FacultyResponseDTO(
                    faculty.getFacultyId(),
                    faculty.getName(),
                    faculty.getEmail(),
                    faculty.getDepartment(),
                    faculty.getCollege().getCollegeCode(),
                    faculty.getCreatedBy().getName(),
                    faculty.getMaxHoursPerDay(),
                    faculty.getMaxHoursPerWeek(),
                    faculty.getDob(),
                    faculty.getQualification(),
                    faculty.getIsActive()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "faculty", responseDTO
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
