package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.EnableNotificationRequest;
import com.example.ChronoFlex.dto.GlobalToggleRequest;
import com.example.ChronoFlex.service.FacultyNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculty/notifications")
@CrossOrigin(origins = "*")
public class FacultyNotificationController {

    private final FacultyNotificationService notificationService;

    public FacultyNotificationController(FacultyNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ============================================================
    // ENABLE / DISABLE NOTIFICATIONS
    // ============================================================
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleNotifications(@RequestBody EnableNotificationRequest req) {
        try {
            String message = notificationService.updateNotificationSetting(req);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/toggle-all")
    public ResponseEntity<?> toggleAll(@RequestBody GlobalToggleRequest req) {
        try {
            String msg = notificationService.updateAllFacultyNotifications(req);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    // ============================================================
    // CHECK CURRENT NOTIFICATION STATUS FOR A FACULTY
    // ============================================================
    @GetMapping("/status/{facultyId}")
    public ResponseEntity<?> checkStatus(@PathVariable Long facultyId) {
        var setting = notificationService.getStatus(facultyId);
        return ResponseEntity.ok(setting);
    }
}
