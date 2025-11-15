package com.example.ChronoFlex.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class RestoreDTOs {

    public static class BackupSummary {
        private Long backupId;
        private LocalDateTime backedUpAt;
        private Long adminId;
        private Long collegeId;
        private String semester;
        private String section;
        private Integer slotsCount;

        // getters/setters
        public Long getBackupId() { return backupId; }
        public void setBackupId(Long backupId) { this.backupId = backupId; }
        public LocalDateTime getBackedUpAt() { return backedUpAt; }
        public void setBackedUpAt(LocalDateTime backedUpAt) { this.backedUpAt = backedUpAt; }
        public Long getAdminId() { return adminId; }
        public void setAdminId(Long adminId) { this.adminId = adminId; }
        public Long getCollegeId() { return collegeId; }
        public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        public Integer getSlotsCount() { return slotsCount; }
        public void setSlotsCount(Integer slotsCount) { this.slotsCount = slotsCount; }
    }

    public static class ConflictDetail {
        private String day;
        private String startTime;
        private String endTime;
        private String reason;
        private Long conflictingAvailabilityId; // nullable

        // getters/setters
        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Long getConflictingAvailabilityId() { return conflictingAvailabilityId; }
        public void setConflictingAvailabilityId(Long conflictingAvailabilityId) { this.conflictingAvailabilityId = conflictingAvailabilityId; }
    }

    public static class PreviewResponse {
        private Long backupId;
        private Long collegeId;
        private String semester;
        private String section;
        private int totalSlots;
        private int conflictCount;
        private List<ConflictDetail> conflicts;

        // getters/setters
        public Long getBackupId() { return backupId; }
        public void setBackupId(Long backupId) { this.backupId = backupId; }
        public Long getCollegeId() { return collegeId; }
        public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        public int getTotalSlots() { return totalSlots; }
        public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }
        public int getConflictCount() { return conflictCount; }
        public void setConflictCount(int conflictCount) { this.conflictCount = conflictCount; }
        public List<ConflictDetail> getConflicts() { return conflicts; }
        public void setConflicts(List<ConflictDetail> conflicts) { this.conflicts = conflicts; }
    }

    public static class RestoreResult {
        private boolean success;
        private String message;
        private int restoredSlots;
        private int skippedSlots;
        private List<ConflictDetail> conflicts;

        // getters/setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getRestoredSlots() { return restoredSlots; }
        public void setRestoredSlots(int restoredSlots) { this.restoredSlots = restoredSlots; }
        public int getSkippedSlots() { return skippedSlots; }
        public void setSkippedSlots(int skippedSlots) { this.skippedSlots = skippedSlots; }
        public List<ConflictDetail> getConflicts() { return conflicts; }
        public void setConflicts(List<ConflictDetail> conflicts) { this.conflicts = conflicts; }
    }
}
