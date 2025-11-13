package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private EmailService emailService;

    private Map<String, Admin> pendingAdmins = new ConcurrentHashMap<>();
    private Map<String, String> collegeOtpMap = new ConcurrentHashMap<>();
    private Map<String, String> adminOtpMap = new ConcurrentHashMap<>();

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Map<String, String> passwordResetOtpMap = new ConcurrentHashMap<>();

    // ============================================================
    // 1️⃣ Initiate Admin Registration
    // ============================================================
    public void initiateAdminRegistration(Admin admin) throws IOException {
        // 1. Check if admin already exists
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Admin already registered with this email.");
        }

        // 2. Validate college code
        College college = collegeRepository.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid college code."));

        // 3. Generate OTP for college
        String otp = generateOTP();
        pendingAdmins.put(admin.getEmail(), admin);
        collegeOtpMap.put(admin.getCollegeCode(), otp);

        // 4. Send OTP to college email
        Map<String, String> values = Map.of(
                "Name", college.getCollegeName(),
                "OTP", otp
        );

        emailService.sendEmailFromTemplate(
                "college_admin_verification_otp.txt",
                college.getContactEmail(),
                values
        );

        // 5. Log the event
        emailService.logCollegeOtpEmail(college.getContactEmail(), college.getCollegeCode(), otp);
    }

    // ============================================================
    // 2️⃣ Verify College OTP → Send Admin OTP
    // ============================================================
    public boolean verifyCollegeOtp(String collegeCode, String otp) throws IOException {
        String savedOtp = collegeOtpMap.get(collegeCode);

        if (savedOtp != null && savedOtp.equals(otp)) {
            // OTP matched → Send OTP to admin’s email
            Admin admin = pendingAdmins.values().stream()
                    .filter(a -> a.getCollegeCode().equals(collegeCode))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No pending admin for this college."));

            String adminOtp = generateOTP();
            adminOtpMap.put(admin.getEmail(), adminOtp);

            Map<String, String> values = Map.of(
                    "Name", admin.getName(),
                    "OTP", adminOtp
            );

            emailService.sendEmailFromTemplate(
                    "admin_final_verification_otp.txt",
                    admin.getEmail(),
                    values
            );

            // Log event
            emailService.logAdminOtpEmail(admin.getEmail(), admin.getName(), adminOtp);

            collegeOtpMap.remove(collegeCode);
            return true;
        }

        // Log failed attempt
        emailService.logAdminRegistrationFailure("college-code-" + collegeCode, "Invalid college OTP entered");
        return false;
    }

    // ============================================================
    // 3️⃣ Verify Admin OTP → Final Registration
    // ============================================================
    public boolean verifyAdminOtp(String email, String otp) {
        String savedOtp = adminOtpMap.get(email);

        if (savedOtp != null && savedOtp.equals(otp)) {
            Admin admin = pendingAdmins.get(email);

            // Hash password before saving
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            admin.setVerified(true);

            adminRepository.save(admin);

            // Log success
            emailService.logAdminRegistrationSuccess(admin.getEmail(), admin.getName(), admin.getCollegeCode());

            // Clean up
            adminOtpMap.remove(email);
            pendingAdmins.remove(email);
            return true;
        }

        // Log failed attempt
        emailService.logAdminRegistrationFailure(email, "Invalid Admin OTP entered");
        return false;
    }

    // ============================================================
    // 4️⃣ Admin Login
    // ============================================================
    public Admin loginAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No admin found with this email"));

        if (!admin.isVerified()) {
            throw new IllegalArgumentException("Admin not verified. Please complete verification.");
        }

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return admin;
    }

    // ============================================================
    // Helper Method → OTP Generator
    // ============================================================
    private String generateOTP() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    // ============================================================
    // 1️⃣ Send Password Reset OTP
    // ============================================================
    public void sendPasswordResetOtp(String email) throws IOException {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isEmpty()) {
            throw new IllegalArgumentException("No admin found with this email.");
        }

        Admin admin = adminOpt.get();

        // Generate OTP
        String otp = generateOTP();
        passwordResetOtpMap.put(email, otp);

        // Prepare mail body (can use template if you want)
        String body = "Hello " + admin.getName() + ",\n\n"
                + "Your password reset OTP is: " + otp
                + "\nThis OTP will expire shortly.\n\n"
                + "Regards,\nChronoFlex Team";

        // Send using your existing EmailService
        emailService.sendEmailFromTemplate(
                "admin_password_reset_otp.txt",
                email,
                Map.of("Name", admin.getName(), "OTP", otp)
        );

        // Log email
        emailService.logEmailActivity(email, "Password Reset OTP Sent", "OTP sent for password reset");
    }

    // ============================================================
    // 2️⃣ Verify OTP and Reset Password
    // ============================================================
    public void resetPassword(String email, String otp, String newPassword) {
        String cachedOtp = passwordResetOtpMap.get(email);

        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        // Hash and update new password
        String hashedPassword = passwordEncoder.encode(newPassword);
        admin.setPassword(hashedPassword);
        adminRepository.save(admin);

        // Clear OTP
        passwordResetOtpMap.remove(email);

        // Log password reset success
        emailService.logEmailActivity(email, "Password Reset Successful", "Password successfully updated");
    }


    // 🔹 Fetch college code directly using credentials
    public String getCollegeCodeByCredentials(String email, String password) {
        Admin admin = verifyAdmin(email, password);
        return admin.getCollegeCode();
    }

    // 🔹 Verify admin credentials and return admin details
    public Admin verifyAdmin(String email, String password) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Admin not found");
        }

        Admin admin = adminOpt.get();
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return admin;
    }
}
