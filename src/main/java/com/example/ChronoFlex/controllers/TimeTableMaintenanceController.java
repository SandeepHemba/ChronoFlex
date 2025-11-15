package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.model.TimeTableBackup;
import com.example.ChronoFlex.service.TimeTableMaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable/maintenance")
@CrossOrigin(origins = "*")
public class TimeTableMaintenanceController {

    private final TimeTableMaintenanceService maintenanceService;

    public TimeTableMaintenanceController(TimeTableMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /**
     * POST /api/timetable/maintenance/delete
     * Body: { "adminEmail": "...", "adminPassword": "...", "semester": "3", "section": "A" }
     *
     * This will backup and delete timetable slots for the class belonging to the admin's college.
     */
    @PostMapping("/delete")
    public ResponseEntity<?> deleteTimetable(@RequestBody DeleteRequest req) {
        try {
            String res = maintenanceService.backupAndDeleteTimetable(
                    req.getAdminEmail(),
                    req.getAdminPassword(),
                    req.getSemester(),
                    req.getSection()
            );
            return ResponseEntity.ok(new Resp("success", res));
        } catch (IllegalAccessException iae) {
            return ResponseEntity.status(401).body(new Resp("unauthorized", iae.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Resp("error", e.getMessage()));
        }
    }

    /**
     * POST /api/timetable/maintenance/delete-history
     * Body: { "adminEmail": "...", "adminPassword": "...", "semester": "3", "section": "A" }
     *
     * Returns backups (delete history) for that class/semester.
     */
    @PostMapping("/delete-history")
    public ResponseEntity<?> getDeleteHistory(@RequestBody DeleteRequest req) {
        try {
            List<TimeTableBackup> history = maintenanceService.getDeleteHistory(
                    req.getAdminEmail(),
                    req.getAdminPassword(),
                    req.getSemester(),
                    req.getSection()
            );
            return ResponseEntity.ok(history);
        } catch (IllegalAccessException iae) {
            return ResponseEntity.status(401).body(new Resp("unauthorized", iae.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new Resp("error", e.getMessage()));
        }
    }

    // Request DTO
    public static class DeleteRequest {
        private String adminEmail;
        private String adminPassword;
        private String semester;
        private String section;

        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }

        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
    }

    // Response wrapper
    public static class Resp {
        private String status;
        private String message;
        public Resp(String status, String message) { this.status = status; this.message = message; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
