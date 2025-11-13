package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.CollegeClassRequest;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.CollegeClass;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeClassRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollegeClassService {

    @Autowired
    private CollegeClassRepository classRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // --------------------------
    // Create a new class
    // --------------------------
    public CollegeClass addClass(CollegeClassRequest request) {

        // Validate admin credentials
        Admin admin = adminRepository.findByEmail(request.getAdminEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(request.getAdminPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid admin credentials");
        }

        // Fetch College
        College college = collegeRepository.findByCollegeCode(request.getCollegeCode())
                .orElseThrow(() -> new RuntimeException("College not found"));

        // Convert numeric semester to Roman
        String romanSemester = convertToRomanSemester(request.getSemester());

        // Create CollegeClass entity
        CollegeClass collegeClass = new CollegeClass();
        collegeClass.setDepartment(request.getDepartment());
        collegeClass.setCollege(college);
        collegeClass.setClassName(request.getClassName());
        collegeClass.setSemester(romanSemester); // store Roman
        collegeClass.setAcademicYear(request.getAcademicYear());
        collegeClass.setSection(request.getSection());
        collegeClass.setTotalStudents(request.getTotalStudents() != null ? request.getTotalStudents() : 0);
        collegeClass.setRoomNumber(request.getRoomNumber());
        if (request.getShift() != null && !request.getShift().isEmpty()) {
            collegeClass.setShift(CollegeClass.Shift.valueOf(request.getShift().toUpperCase()));
        }

        // Set createdBy automatically to Admin (or Admin converted to Faculty if needed)
        collegeClass.setCreatedBy(admin); // Admin linked as creator

        collegeClass.setCreatedAt(LocalDateTime.now());

        return classRepository.save(collegeClass);
    }

    // --------------------------
    // Fetch classes
    // --------------------------
    public List<CollegeClass> fetchClasses(String adminEmail, String adminPassword,
                                           String collegeCode, String semester,
                                           CollegeClass.Status status) {

        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new RuntimeException("Invalid admin credentials");
        }

        College college = null;
        if (collegeCode != null && !collegeCode.isEmpty()) {
            college = collegeRepository.findByCollegeCode(collegeCode)
                    .orElseThrow(() -> new RuntimeException("College not found"));
        }

        // Convert numeric semester to Roman before querying
        String romanSemester = convertToRomanSemester(semester);

        if (college != null && romanSemester != null && status != null) {
            return classRepository.findByCollegeAndSemesterAndStatus(college, romanSemester, status);
        } else if (college != null && romanSemester != null) {
            return classRepository.findByCollegeAndSemester(college, romanSemester);
        } else if (college != null && status != null) {
            return classRepository.findByCollegeAndStatus(college, status);
        } else if (college != null) {
            return classRepository.findByCollege(college);
        } else {
            return classRepository.findAll();
        }
    }

    // --------------------------
    // Helper: Convert numeric semester to Roman
    // --------------------------
    private String convertToRomanSemester(String numeric) {
        if (numeric == null) return null;
        return switch (numeric) {
            case "1" -> "I";
            case "2" -> "II";
            case "3" -> "III";
            case "4" -> "IV";
            case "5" -> "V";
            case "6" -> "VI";
            case "7" -> "VII";
            case "8" -> "VIII";
            default -> numeric; // fallback
        };
    }


    // --------------------------
    // Edit a class (Admin only)
    // --------------------------
    public CollegeClass editClass(Integer classId, CollegeClassRequest request) {
        Admin admin = validateAdmin(request.getAdminEmail(), request.getAdminPassword());

        CollegeClass collegeClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (request.getDepartment() != null) collegeClass.setDepartment(request.getDepartment());
        if (request.getClassName() != null) collegeClass.setClassName(request.getClassName());
        if (request.getSemester() != null) collegeClass.setSemester(convertToRomanSemester(request.getSemester()));
        if (request.getAcademicYear() != null) collegeClass.setAcademicYear(request.getAcademicYear());
        if (request.getSection() != null) collegeClass.setSection(request.getSection());
        if (request.getTotalStudents() != null) collegeClass.setTotalStudents(request.getTotalStudents());
        if (request.getRoomNumber() != null) collegeClass.setRoomNumber(request.getRoomNumber());
        if (request.getShift() != null && !request.getShift().isEmpty())
            collegeClass.setShift(CollegeClass.Shift.valueOf(request.getShift().toUpperCase()));

        collegeClass.setUpdatedAt(LocalDateTime.now());

        return classRepository.save(collegeClass);
    }

    // --------------------------
    // Delete a class (Admin only)
    // --------------------------
    public String deleteClass(Integer classId, String adminEmail, String adminPassword) {
        validateAdmin(adminEmail, adminPassword);

        CollegeClass collegeClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        classRepository.delete(collegeClass);
        return "Class deleted successfully.";
    }

    // --------------------------
    // Helper: Validate Admin
    // --------------------------
    private Admin validateAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid admin credentials");
        }
        return admin;
    }

    // --------------------------
    // Helper: Get College by Code
    // --------------------------
    private College getCollegeByCode(String code) {
        return collegeRepository.findByCollegeCode(code)
                .orElseThrow(() -> new RuntimeException("College not found"));
    }
}
