package com.example.ChronoFlex.dto;

public class TimeTableRequest {
    private Long collegeId;
    private String semesterNumber;
    private String section;
    private Integer templateId;
    private Long adminId;

    // Getters and setters
    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }

    public String getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(String semesterNumber) { this.semesterNumber = semesterNumber; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
}
