package com.example.ChronoFlex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "timetable_backup",
        indexes = {
                @Index(name = "idx_tb_college", columnList = "college_id"),
                @Index(name = "idx_tb_sem_section", columnList = "semester, section"),
                @Index(name = "idx_tb_faculty", columnList = "faculty_id")
        })
public class TimeTableBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Original availability fields */
    private Long originalAvailabilityId;
    private Long facultyId;
    @Enumerated(EnumType.STRING)
    private FacultyAvailability.DayOfWeekEnum dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer classId;
    private Long subjectId;
    private String semester;
    private String section;
    private Integer templateId;
    @Enumerated(EnumType.STRING)
    private FacultyAvailability.Status status;
    private Long collegeId;
    private Long createdBy; // original admin who created the slot
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* Backup metadata */
    private Long backupByAdminId;
    private LocalDateTime backedUpAt;

    public TimeTableBackup() {}

    // Simple constructor for mapping
    public TimeTableBackup(Long originalAvailabilityId,
                           Long facultyId,
                           FacultyAvailability.DayOfWeekEnum dayOfWeek,
                           LocalTime startTime,
                           LocalTime endTime,
                           Integer classId,
                           Long subjectId,
                           String semester,
                           String section,
                           Integer templateId,
                           FacultyAvailability.Status status,
                           Long collegeId,
                           Long createdBy,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           Long backupByAdminId,
                           LocalDateTime backedUpAt) {
        this.originalAvailabilityId = originalAvailabilityId;
        this.facultyId = facultyId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classId = classId;
        this.subjectId = subjectId;
        this.semester = semester;
        this.section = section;
        this.templateId = templateId;
        this.status = status;
        this.collegeId = collegeId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.backupByAdminId = backupByAdminId;
        this.backedUpAt = backedUpAt;
    }

    // Getters & setters (generate in IDE) - minimal ones added for usage

    public Long getId() { return id; }
    public Long getOriginalAvailabilityId() { return originalAvailabilityId; }
    public void setOriginalAvailabilityId(Long originalAvailabilityId) { this.originalAvailabilityId = originalAvailabilityId; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public FacultyAvailability.DayOfWeekEnum getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(FacultyAvailability.DayOfWeekEnum dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public FacultyAvailability.Status getStatus() { return status; }
    public void setStatus(FacultyAvailability.Status status) { this.status = status; }

    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getBackupByAdminId() { return backupByAdminId; }
    public void setBackupByAdminId(Long backupByAdminId) { this.backupByAdminId = backupByAdminId; }

    public LocalDateTime getBackedUpAt() { return backedUpAt; }
    public void setBackedUpAt(LocalDateTime backedUpAt) { this.backedUpAt = backedUpAt; }

    // Add this getter to match your service code
    public Long getBackedUpBy() {
        return backupByAdminId;
    }


}
