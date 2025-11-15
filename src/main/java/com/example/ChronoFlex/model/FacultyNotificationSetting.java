package com.example.ChronoFlex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_notification_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_id"}))
public class FacultyNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHICH faculty this setting belongs to
    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    // Belongs to which college (admin restriction)
    @Column(name = "college_id", nullable = false)
    private Long collegeId;

    // Enable/Disable reminder emails
    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public FacultyNotificationSetting() {}

    public FacultyNotificationSetting(Long facultyId, Long collegeId, boolean enabled) {
        this.facultyId = facultyId;
        this.collegeId = collegeId;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }
    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
