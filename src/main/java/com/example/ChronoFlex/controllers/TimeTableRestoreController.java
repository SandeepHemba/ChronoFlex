package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.RestoreDTOs.*;
import com.example.ChronoFlex.service.TimeTableRestoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable/backup")
@CrossOrigin(origins = "*")
public class TimeTableRestoreController {

    private final TimeTableRestoreService restoreService;

    public TimeTableRestoreController(TimeTableRestoreService restoreService) {
        this.restoreService = restoreService;
    }

    /**
     * List backups for semester+section (admin auth via email+pass)
     * Example: GET /api/timetable/backup/list?adminEmail=...&adminPassword=...&semester=3&section=A
     */
    @PostMapping("/list")
    public ResponseEntity<?> listBackups(@RequestBody ListRequest req) {
        try {
            List<BackupSummary> list =
                    restoreService.listBackups(
                            req.getAdminEmail(),
                            req.getAdminPassword(),
                            req.getSemester(),
                            req.getSection()
                    );

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }



    /**
     * Preview backup (conflict check) by backupId
     * POST body: { "adminEmail":"x","adminPassword":"y","backupId": 123 }
     */
    @PostMapping("/preview")
    public ResponseEntity<?> previewRestore(@RequestBody PreviewRequest req) {
        try {
            PreviewResponse resp = restoreService.previewRestore(req.getAdminEmail(), req.getAdminPassword(), req.getBackupId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * Restore backup (strict): if any conflicts, restore is blocked.
     * POST body: { "adminEmail":"x","adminPassword":"y","backupId": 123 }
     */
    @PostMapping("/restore")
    public ResponseEntity<?> restore(@RequestBody PreviewRequest req) {
        try {
            RestoreResult res = restoreService.restoreBackup(req.getAdminEmail(), req.getAdminPassword(), req.getBackupId());
            if (!res.isSuccess()) return ResponseEntity.status(409).body(res); // conflict
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Request body DTO for preview/restore
    public static class PreviewRequest {
        private String adminEmail;
        private String adminPassword;
        private Long backupId;

        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
        public Long getBackupId() { return backupId; }
        public void setBackupId(Long backupId) { this.backupId = backupId; }
    }

    public static class ListRequest {
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

}
