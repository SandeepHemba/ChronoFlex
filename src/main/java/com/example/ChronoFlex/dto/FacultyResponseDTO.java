package com.example.ChronoFlex.dto;

public class FacultyResponseDTO {
    private Long facultyId;
    private String name;
    private String email;
    private String department;
    private String collegeCode; // college code instead of full entity
    private String createdByName; // admin name instead of full entity
    private Integer maxHoursPerDay;
    private Integer maxHoursPerWeek;
    private String dob;
    private String qualification;
    private Boolean isActive;

    public FacultyResponseDTO(Long facultyId, String name, String email, String department,
                              String collegeCode, String createdByName, Integer maxHoursPerDay,
                              Integer maxHoursPerWeek, String dob, String qualification,
                              Boolean isActive) {
        this.facultyId = facultyId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.collegeCode = collegeCode;
        this.createdByName = createdByName;
        this.maxHoursPerDay = maxHoursPerDay;
        this.maxHoursPerWeek = maxHoursPerWeek;
        this.dob = dob;
        this.qualification = qualification;
        this.isActive = isActive;
    }

    // Getters and setters

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = collegeCode;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Integer getMaxHoursPerDay() {
        return maxHoursPerDay;
    }

    public void setMaxHoursPerDay(Integer maxHoursPerDay) {
        this.maxHoursPerDay = maxHoursPerDay;
    }

    public Integer getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public void setMaxHoursPerWeek(Integer maxHoursPerWeek) {
        this.maxHoursPerWeek = maxHoursPerWeek;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
