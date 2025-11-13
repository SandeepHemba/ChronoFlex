package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultySubjectMapService {

    @Autowired
    private FacultySubjectMapRepository facultySubjectMapRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private FacultySubjectMapRepository mapRepository;  // ✅ must be autowired

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private String mapSemesterInput(String semester) {
        if(semester == null) return null;
        return switch(semester) {
            case "1" -> "I Sem";
            case "2" -> "II Sem";
            case "3" -> "III Sem";
            case "4" -> "IV Sem";
            case "5" -> "V Sem";
            case "6" -> "VI Sem";
            default -> semester;
        };
    }


    // ============================================================
    // Map Faculty to Subject (Admin Only)
    // ============================================================
    public FacultySubjectMap mapFacultyToSubject(String adminEmail, String adminPassword,
                                                 String facultyName, String facultyEmail,
                                                 String facultyDob, String subjectCode,
                                                 Integer weeklyHours, String preferredDays,
                                                 String notes) {

        // 1️⃣ Authenticate Admin
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Invalid admin email"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword()))
            throw new RuntimeException("Invalid admin credentials");

        if (!admin.isVerified())
            throw new RuntimeException("Admin not verified");

        // 2️⃣ Get College from admin
        College college = collegeRepository.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new RuntimeException("Invalid college code"));

        // 3️⃣ Find Faculty by name
        List<Faculty> matchingFaculties = facultyRepository.findByNameAndCollege(facultyName, college);
        if (matchingFaculties.isEmpty())
            throw new RuntimeException("No faculty found with that name in this college");

        Faculty faculty;
        if (matchingFaculties.size() > 1) {
            if (facultyEmail != null) {
                faculty = matchingFaculties.stream()
                        .filter(f -> f.getEmail().equalsIgnoreCase(facultyEmail))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No faculty found with given email"));
            } else if (facultyDob != null) {
                faculty = matchingFaculties.stream()
                        .filter(f -> f.getDob() != null && f.getDob().toString().equals(facultyDob))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No faculty found with given DOB"));
            } else {
                throw new RuntimeException("Multiple faculties found. Please provide email or DOB.");
            }
        } else {
            faculty = matchingFaculties.get(0);
        }

        // 4️⃣ Get Subject by code
        Subject subject = (Subject) subjectRepository.findBySubjectCodeAndCollege(subjectCode, college)
                .orElseThrow(() -> new RuntimeException("Subject not found for this code in your college"));

        // 5️⃣ Check duplicate mapping
        boolean exists = facultySubjectMapRepository.existsByFacultyAndSubject(faculty, subject);
        if (exists)
            throw new RuntimeException("Faculty already mapped to this subject");

        // 6️⃣ Create mapping (semester & department auto from subject)
        FacultySubjectMap map = new FacultySubjectMap();
        map.setCollege(college);
        map.setFaculty(faculty);
        map.setSubject(subject);
        map.setWeeklyHours(weeklyHours != null ? weeklyHours : 0);
        map.setPreferredDays(preferredDays);
        map.setSemester(subject.getSemester());
        map.setDepartment(subject.getDepartment());
        map.setCreatedBy(admin);
        map.setCreatedAt(LocalDateTime.now());
        map.setNotes(notes);
        map.setIsActive(true);

        return facultySubjectMapRepository.save(map);
    }

    // =======================
    // Fetch all mappings (Admin only)
    // =======================
    public List<FacultySubjectMap> fetchAllMappings(String adminEmail, String adminPassword,
                                                    String department, String semester, Boolean active) {

        // 1️⃣ Authenticate admin
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid admin email"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid admin credentials");
        }

        if (!admin.isVerified()) {
            throw new IllegalArgumentException("Admin not verified");
        }

        // 2️⃣ Get college
        College college = collegeRepository.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid college code"));

        // 3️⃣ Fetch all mappings for the college
        List<FacultySubjectMap> mappings = mapRepository.findByCollege(college);

        // 4️⃣ Apply optional filters safely
        String mappedSemester = mapSemesterInput(semester);
        return mappings.stream()
                .filter(m -> mappedSemester == null || m.getSubject().getSemester().equalsIgnoreCase(mappedSemester))
                .filter(m -> department == null || m.getSubject().getDepartment().equalsIgnoreCase(department))
                .filter(m -> active == null || m.getIsActive().equals(active))
                .toList();

    }

}
