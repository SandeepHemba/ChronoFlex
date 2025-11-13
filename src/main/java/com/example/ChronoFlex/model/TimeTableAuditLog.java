package com.example.ChronoFlex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_audit_logs")
public class TimeTableAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;
    private Long collegeId;
    private Integer templateId;
    private String templateName;
    private String semester;
    private String section;
    private LocalDateTime generatedAt;
    private String status;
    private String message;

    public TimeTableAuditLog() {}

    public TimeTableAuditLog(Long adminId, Long collegeId, Integer templateId, String templateName,
                             String semester, String section, String status, String message) {
        this.adminId = adminId;
        this.collegeId = collegeId;
        this.templateId = templateId;
        this.templateName = templateName;
        this.semester = semester;
        this.section = section;
        this.status = status;
        this.message = message;
        this.generatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public Long getCollegeId() { return collegeId; }
    public Integer getTemplateId() { return templateId; }
    public String getTemplateName() { return templateName; }
    public String getSemester() { return semester; }
    public String getSection() { return section; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }

    public void setId(Long id) { this.id = id; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setSection(String section) { this.section = section; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
}
