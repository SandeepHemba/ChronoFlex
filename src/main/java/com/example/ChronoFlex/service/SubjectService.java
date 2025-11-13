package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.SubjectRequestDTO;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Subject;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import com.example.ChronoFlex.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ----------------- Add Subject -----------------
    public Subject addSubject(SubjectRequestDTO dto) {
        Admin admin = authenticateAdmin(dto.getAdminEmail(), dto.getAdminPassword());

        College college = collegeRepository.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid college code"));

        Subject subject = new Subject();
        subject.setCollege(college);
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectType(dto.getSubjectType());
        subject.setSemester(dto.getSemester());
        subject.setDepartment(dto.getDepartment());
        subject.setCredits(dto.getCredits());
        subject.setDescription(dto.getDescription());
        subject.setCreatedBy(admin);
        subject.setIsActive(true);
        subject.setCreatedAt(LocalDateTime.now());
        subject.setUpdatedAt(LocalDateTime.now());

        return subjectRepository.save(subject);
    }

    // ----------------- Fetch Subjects -----------------
    public List<Subject> fetchSubjects(SubjectRequestDTO dto) {
        Admin admin = authenticateAdmin(dto.getAdminEmail(), dto.getAdminPassword());

        College college = collegeRepository.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid college code"));

        return subjectRepository.findAll().stream()
                .filter(s -> s.getCollege().getCollegeId().equals(college.getCollegeId()))
                .filter(s -> dto.getFilterDepartment() == null || s.getDepartment().equalsIgnoreCase(dto.getFilterDepartment()))
                .filter(s -> dto.getFilterSemester() == null || s.getSemester().equalsIgnoreCase(dto.getFilterSemester()))
                .filter(s -> dto.getFilterSubjectType() == null || s.getSubjectType() == dto.getFilterSubjectType())
                .filter(s -> dto.getIncludeInactive() == null || dto.getIncludeInactive() || s.getIsActive())
                .toList();
    }

    // ----------------- Activate/Deactivate Subject -----------------
    public Subject toggleSubjectStatus(Long subjectId, boolean active, String adminEmail, String adminPassword) {
        Admin admin = authenticateAdmin(adminEmail, adminPassword);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        subject.setIsActive(active);
        subject.setUpdatedAt(LocalDateTime.now());

        return subjectRepository.save(subject);
    }

    // ----------------- Helper: Admin Authentication -----------------
    private Admin authenticateAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid admin email"));
        if (!passwordEncoder.matches(password, admin.getPassword()))
            throw new IllegalArgumentException("Invalid admin credentials");
        if (!admin.isVerified())
            throw new IllegalArgumentException("Admin not verified");
        return admin;
    }
}
