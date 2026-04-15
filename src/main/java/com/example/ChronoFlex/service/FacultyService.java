package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.FacultyResponseDTO;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Faculty;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import com.example.ChronoFlex.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EmailService emailService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Faculty registerFacultyByAdmin(String adminEmail, String adminPassword, String facultyName,
                                          String facultyEmail, String department, String collegeCode,
                                          Integer maxHoursPerDay, Integer maxHoursPerWeek,
                                          String dob, String qualification) throws IOException {
        // 1️⃣ Validate Admin
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid admin email"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid admin password");
        }

        if (!admin.isVerified()) {
            throw new IllegalArgumentException("Admin not verified");
        }

        // 2️⃣ Validate College
        College college = collegeRepository.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid college code"));

        // 3️⃣ Check if faculty email already exists in this college
        if (facultyRepository.findByEmailAndCollege_CollegeId(facultyEmail, college.getCollegeId()).isPresent()) {
            throw new IllegalArgumentException("Faculty already exists in this college with this email");
        }

        // 4️⃣ Generate default password - old version
        //String defaultPassword = college.getCollegeCode() + department + "@123";

        // 4️⃣ Generate default password (CollegeCode + CleanDept + @ + DOB) - new version - 27th feb 2026
        if (dob == null || dob.isEmpty()) {
            throw new IllegalArgumentException("DOB is required to generate default password");
        }

        // Remove spaces from department
        String cleanDept = department.replaceAll("\\s+", "");

        // Remove dashes from DOB (yyyy-mm-dd → yyyymmdd)
        String formattedDob = dob.replace("-", "");

        // Final password format
        String defaultPassword = college.getCollegeCode() + cleanDept + "@" + formattedDob;

        // 5️⃣ Create faculty object
        Faculty faculty = new Faculty();
        faculty.setName(facultyName);
        faculty.setEmail(facultyEmail);
        faculty.setDepartment(department);
        faculty.setPassword(passwordEncoder.encode(defaultPassword));
        faculty.setCollege(college);
        faculty.setCreatedBy(admin);
        faculty.setIsActive(true);
        faculty.setMaxHoursPerDay(maxHoursPerDay);
        faculty.setMaxHoursPerWeek(maxHoursPerWeek);

        // Optional fields
        if (dob != null && !dob.isEmpty()) {
            faculty.setDob(String.valueOf(LocalDate.parse(dob)));
        }
        faculty.setQualification(qualification);

        // 6️⃣ Save faculty
        Faculty savedFaculty = facultyRepository.save(faculty);

        // 7️⃣ Send email
        emailService.sendEmailFromTemplate("faculty_registration_email.html", facultyEmail,
                java.util.Map.of("Name", facultyName, "Password", defaultPassword));

        // 8️⃣ Log email
        emailService.logEmailActivity(facultyEmail, "Faculty Registration", "Faculty account created");

        return savedFaculty;
    }

    // Temporary storage for OTPs
    private Map<String, String> passwordResetOtpMap = new ConcurrentHashMap<>();

    // ============================================================
    // 1️⃣ Send Password Reset OTP
    // ============================================================
    public void sendPasswordResetOtp(String email) throws IOException {
        Optional<Faculty> facultyOpt = facultyRepository.findByEmail(email);

        if (facultyOpt.isEmpty()) {
            throw new IllegalArgumentException("No faculty found with this email.");
        }

        Faculty faculty = facultyOpt.get();

        // Generate OTP
        String otp = generateOTP();
        passwordResetOtpMap.put(email, otp);

        // Send OTP email using template
        emailService.sendEmailFromTemplate("faculty_password_reset_otp.html",
                email, Map.of("Name", faculty.getName(), "OTP", otp));

        // Log email
        emailService.logEmailActivity(email, "Faculty Password Reset OTP Sent",
                "OTP sent for password reset");
    }

    // ============================================================
    // 2️⃣ Verify OTP and Reset Password
    // ============================================================
    public void resetPassword(String email, String otp, String newPassword) {
        String cachedOtp = passwordResetOtpMap.get(email);

        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            // Log failure
            emailService.logEmailActivity(email, "Faculty Password Reset Failed",
                    "Invalid or expired OTP");
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        Faculty faculty = facultyRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found."));

        // Hash and update new password
        String hashedPassword = passwordEncoder.encode(newPassword);
        faculty.setPassword(hashedPassword);
        facultyRepository.save(faculty);

        // Clear OTP
        passwordResetOtpMap.remove(email);

        // Log success
        emailService.logEmailActivity(email, "Faculty Password Reset Successful",
                "Password successfully updated");
    }

    // ============================================================
    // Helper → OTP Generator
    // ============================================================
    private String generateOTP() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }


    public List<FacultyResponseDTO> getFacultiesByAdmin(
            String adminEmail,
            String password,
            String department,
            String qualification,
            Boolean active
    ) {
        // 1️⃣ Authenticate Admin
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid admin email."));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid admin credentials.");
        }

        if (!admin.isVerified()) {
            throw new IllegalArgumentException("Admin not verified.");
        }

        // 2️⃣ Get College Code from admin
        String collegeCode = admin.getCollegeCode();
        if (collegeCode == null || collegeCode.isEmpty()) {
            throw new IllegalArgumentException("Admin is not associated with any college code.");
        }

        // 3️⃣ Get College entity
        College college = collegeRepository.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid college code associated with admin."));

        // 4️⃣ Fetch faculties with filters
        List<Faculty> faculties = facultyRepository.findFilteredFaculties(
                college.getCollegeId(),
                department,
                qualification,
                active
        );

        // 5️⃣ Convert to FacultyResponseDTO
        return faculties.stream().map(faculty -> new FacultyResponseDTO(
                faculty.getFacultyId(),
                faculty.getName(),
                faculty.getEmail(),
                faculty.getDepartment(),
                faculty.getCollege().getCollegeCode(),              // ✅ College code only
                faculty.getCreatedBy().getName(),                   // ✅ Admin name only
                faculty.getMaxHoursPerDay(),
                faculty.getMaxHoursPerWeek(),
                faculty.getDob() != null ? faculty.getDob().toString() : null,
                faculty.getQualification(),
                faculty.getIsActive()
        )).toList();
    }

    // ============================================================
    // 🔹 Faculty Login
    // ============================================================
    public Faculty loginFaculty(String email, String password) {
        Optional<Faculty> facultyOpt = facultyRepository.findByEmail(email);

        if (facultyOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Faculty faculty = facultyOpt.get();

        if (!passwordEncoder.matches(password, faculty.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!faculty.getIsActive()) {
            throw new IllegalArgumentException("Faculty account is inactive.");
        }

        return faculty;
    }

    public Faculty getFacultyByEmail(String email) {
        return facultyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found with email: " + email));
    }


}
