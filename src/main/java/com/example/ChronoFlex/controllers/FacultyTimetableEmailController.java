package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.service.FacultyTimetableEmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculty/timetable")
@CrossOrigin(origins = "*")
public class FacultyTimetableEmailController {

    private final FacultyTimetableEmailService emailService;

    public FacultyTimetableEmailController(FacultyTimetableEmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email-all")
    public ResponseEntity<?> sendAllFacultyTimetables(@RequestBody AdminAuthRequest request) {
        try {
            int sentCount = emailService.sendAllFacultyTimetables(
                    request.getAdminEmail(),
                    request.getAdminPassword()
            );

            return ResponseEntity.ok(new ResponseMessage(
                    "success",
                    "Timetables emailed successfully to " + sentCount + " faculties."
            ));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body(new ResponseMessage("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ResponseMessage("error", e.getMessage()));
        }
    }

    // DTO for admin credentials
    public static class AdminAuthRequest {
        private String adminEmail;
        private String adminPassword;

        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    }

    // Response structure
    public static class ResponseMessage {
        private String status;
        private String message;

        public ResponseMessage(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
