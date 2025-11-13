package com.example.ChronoFlex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer templateId;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 100)
    private String templateName;

    @Column(nullable = false, length = 100)
    private String workingDays; // Example: "Mon,Tue,Wed,Thu,Fri"

    @Column(nullable = false)
    private Integer classesPerDay;

    @Column(nullable = false)
    private Integer durationPerClass; // Minutes

    @Column(nullable = false)
    private Integer numBreaks;

    @Column(nullable = false)
    private Integer durationOfBreak; // ✅ New field: minutes per break

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime; // ✅ New field: computed or manual end time

    @Column(nullable = true)
    private LocalTime breakTimeFrom; // ✅ Optional: first break start time

    @Column(nullable = true)
    private LocalTime breakTimeTo; // ✅ Optional: first break end time

    @Column(nullable = false)
    private Integer maxHoursPerFacultyPerDay;

    @Column(nullable = false)
    private Integer maxHoursPerFacultyPerWeek;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Admin createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE, INACTIVE
    }

    // ----------------------------
    // Getters & Setters
    // ----------------------------
    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public College getCollege() { return college; }
    public void setCollege(College college) { this.college = college; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

    public Integer getClassesPerDay() { return classesPerDay; }
    public void setClassesPerDay(Integer classesPerDay) { this.classesPerDay = classesPerDay; }

    public Integer getDurationPerClass() { return durationPerClass; }
    public void setDurationPerClass(Integer durationPerClass) { this.durationPerClass = durationPerClass; }

    public Integer getNumBreaks() { return numBreaks; }
    public void setNumBreaks(Integer numBreaks) { this.numBreaks = numBreaks; }

    public Integer getDurationOfBreak() { return durationOfBreak; }
    public void setDurationOfBreak(Integer durationOfBreak) { this.durationOfBreak = durationOfBreak; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalTime getBreakTimeFrom() { return breakTimeFrom; }
    public void setBreakTimeFrom(LocalTime breakTimeFrom) { this.breakTimeFrom = breakTimeFrom; }

    public LocalTime getBreakTimeTo() { return breakTimeTo; }
    public void setBreakTimeTo(LocalTime breakTimeTo) { this.breakTimeTo = breakTimeTo; }

    public Integer getMaxHoursPerFacultyPerDay() { return maxHoursPerFacultyPerDay; }
    public void setMaxHoursPerFacultyPerDay(Integer maxHoursPerFacultyPerDay) { this.maxHoursPerFacultyPerDay = maxHoursPerFacultyPerDay; }

    public Integer getMaxHoursPerFacultyPerWeek() { return maxHoursPerFacultyPerWeek; }
    public void setMaxHoursPerFacultyPerWeek(Integer maxHoursPerFacultyPerWeek) { this.maxHoursPerFacultyPerWeek = maxHoursPerFacultyPerWeek; }

    public Admin getCreatedBy() { return createdBy; }
    public void setCreatedBy(Admin createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }


}
