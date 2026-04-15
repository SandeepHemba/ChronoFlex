package com.example.ChronoFlex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_master_list",
        uniqueConstraints = @UniqueConstraint(columnNames = {"collegeCode", "regId"})
)
public class StudentMasterList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String collegeCode;

    @Column(nullable = false)
    private String regId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false)
    private String section;

    private String officialEmail;

    @Column(nullable = false)
    private boolean isRegistered = false;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Long uploadedByAdminId;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = collegeCode;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUploadedByAdminId() {
        return uploadedByAdminId;
    }

    public void setUploadedByAdminId(Long uploadedByAdminId) {
        this.uploadedByAdminId = uploadedByAdminId;
    }
}