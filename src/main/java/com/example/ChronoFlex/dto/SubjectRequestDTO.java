package com.example.ChronoFlex.dto;

import com.example.ChronoFlex.model.Subject.SubjectType;

public class SubjectRequestDTO {

    // ----------------- Admin Authentication -----------------
    private String adminEmail;
    private String adminPassword;

    // ----------------- Subject Details (for Add) -----------------
    private String subjectCode;
    private String subjectName;
    private SubjectType subjectType;
    private String semester;
    private String department;
    private Integer credits;
    private String description;

    // ----------------- Filters for Fetch -----------------
    private String filterDepartment;
    private String filterSemester;
    private SubjectType filterSubjectType;
    private Boolean includeInactive; // optional: fetch inactive subjects

    // ----------------- Getters and Setters -----------------
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public SubjectType getSubjectType() { return subjectType; }
    public void setSubjectType(SubjectType subjectType) { this.subjectType = subjectType; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFilterDepartment() { return filterDepartment; }
    public void setFilterDepartment(String filterDepartment) { this.filterDepartment = filterDepartment; }

    public String getFilterSemester() { return filterSemester; }
    public void setFilterSemester(String filterSemester) { this.filterSemester = filterSemester; }

    public SubjectType getFilterSubjectType() { return filterSubjectType; }
    public void setFilterSubjectType(SubjectType filterSubjectType) { this.filterSubjectType = filterSubjectType; }

    public Boolean getIncludeInactive() { return includeInactive; }
    public void setIncludeInactive(Boolean includeInactive) { this.includeInactive = includeInactive; }
}
