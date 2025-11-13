package com.example.ChronoFlex.dto;

/**
 * Wrapper DTO containing required identifiers + optional generation filters.
 */
public class TimeTableGenerationRequest {

    private Long collegeId;
    private String semester;   // numeric as string, e.g., "4"
    private String section;    // e.g., "A"
    private Integer templateId;
    private Long adminId;

    private TimeTableGenerationFilters filters;

    // Getters & Setters
    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public TimeTableGenerationFilters getFilters() { return filters; }
    public void setFilters(TimeTableGenerationFilters filters) { this.filters = filters; }
}
