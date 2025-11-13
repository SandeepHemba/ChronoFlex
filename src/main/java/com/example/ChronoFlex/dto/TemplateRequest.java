package com.example.ChronoFlex.dto;

import java.time.LocalTime;

public class TemplateRequest {

    private String adminEmail;
    private String adminPassword;

    private String templateName;
    private String collegeCode;
    private String workingDays; // "Mon,Tue,Wed"
    private Integer classesPerDay;
    private Integer durationPerClass;
    private Integer numBreaks;
    private Integer durationOfBreak;
    private LocalTime breakTimeFrom; // new
    private LocalTime breakTimeTo;   // new
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxHoursPerFacultyPerDay;
    private Integer maxHoursPerFacultyPerWeek;

    // Getters & Setters
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }

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

    public LocalTime getBreakTimeFrom() { return breakTimeFrom; }
    public void setBreakTimeFrom(LocalTime breakTimeFrom) { this.breakTimeFrom = breakTimeFrom; }

    public LocalTime getBreakTimeTo() { return breakTimeTo; }
    public void setBreakTimeTo(LocalTime breakTimeTo) { this.breakTimeTo = breakTimeTo; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getMaxHoursPerFacultyPerDay() { return maxHoursPerFacultyPerDay; }
    public void setMaxHoursPerFacultyPerDay(Integer maxHoursPerFacultyPerDay) { this.maxHoursPerFacultyPerDay = maxHoursPerFacultyPerDay; }

    public Integer getMaxHoursPerFacultyPerWeek() { return maxHoursPerFacultyPerWeek; }
    public void setMaxHoursPerFacultyPerWeek(Integer maxHoursPerFacultyPerWeek) { this.maxHoursPerFacultyPerWeek = maxHoursPerFacultyPerWeek; }
}
