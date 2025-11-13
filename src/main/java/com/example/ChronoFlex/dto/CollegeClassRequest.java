package com.example.ChronoFlex.dto;

public class CollegeClassRequest {

    private String adminEmail;
    private String adminPassword;

    private String collegeCode;
    private String className;
    private String semester;
    private String section;
    private Integer totalStudents;
    private String academicYear;
    private String shift;
    private String roomNumber;
    private String department;      // ✅ Add this

    // ========================
    // Getters & Setters
    // ========================
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getDepartment() { return department; }  // ✅ getter
    public void setDepartment(String department) { this.department = department; } // ✅ setter

}
