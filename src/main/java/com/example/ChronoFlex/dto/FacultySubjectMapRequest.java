package com.example.ChronoFlex.dto;

public class FacultySubjectMapRequest {

    // Admin credentials (mandatory for authorization)
    private String adminEmail;
    private String adminPassword;

    // Faculty info
    private String facultyName;
    private String facultyEmail;  // optional if multiple faculty with same name
    private String facultyDob;    // optional alternate

    // Subject info
    private String subjectCode;

    // Mapping details
    private Integer weeklyHours;
    private String preferredDays; // e.g., "Mon, Wed, Fri"
    private String notes;         // optional


    // Filters for fetching mappings
    private String department;
    private String semester;
    private Boolean active;


    // ============================================================
    // Getters & Setters
    // ============================================================
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getFacultyEmail() { return facultyEmail; }
    public void setFacultyEmail(String facultyEmail) { this.facultyEmail = facultyEmail; }

    public String getFacultyDob() { return facultyDob; }
    public void setFacultyDob(String facultyDob) { this.facultyDob = facultyDob; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }

    public String getPreferredDays() { return preferredDays; }
    public void setPreferredDays(String preferredDays) { this.preferredDays = preferredDays; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }


    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
