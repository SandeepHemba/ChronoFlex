package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.model.TimeTableAuditLog;
import com.example.ChronoFlex.repository.TimeTableAuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable/logs")
@CrossOrigin(origins = "*")
public class TimeTableAuditLogController {

    private final TimeTableAuditLogRepository auditLogRepo;

    public TimeTableAuditLogController(TimeTableAuditLogRepository auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
    }

    // ✅ Fetch all logs
    @GetMapping
    public ResponseEntity<List<TimeTableAuditLog>> getAllLogs() {
        List<TimeTableAuditLog> logs = auditLogRepo.findAll();
        return ResponseEntity.ok(logs);
    }

    // ✅ Fetch logs by adminId
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<TimeTableAuditLog>> getLogsByAdmin(@PathVariable Long adminId) {
        List<TimeTableAuditLog> logs = auditLogRepo.findAll()
                .stream()
                .filter(log -> log.getAdminId().equals(adminId))
                .toList();
        return ResponseEntity.ok(logs);
    }

    // ✅ Fetch logs by college
    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<TimeTableAuditLog>> getLogsByCollege(@PathVariable Long collegeId) {
        List<TimeTableAuditLog> logs = auditLogRepo.findAll()
                .stream()
                .filter(log -> log.getCollegeId().equals(collegeId))
                .toList();
        return ResponseEntity.ok(logs);
    }

    // ✅ Delete all logs (admin use only)
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllLogs() {
        auditLogRepo.deleteAll();
        return ResponseEntity.ok("🧾 All timetable audit logs cleared successfully!");
    }
}
