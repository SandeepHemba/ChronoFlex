package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.Student;
import com.example.ChronoFlex.model.StudentMasterList;
import com.example.ChronoFlex.repository.StudentMasterListRepository;
import com.example.ChronoFlex.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private StudentMasterListRepository masterRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==========================
    // Temporary OTP Storage
    // ==========================
    private Map<String,String> otpStore = new HashMap<>();
    private Map<String, LocalDateTime> otpExpiry = new HashMap<>();


    public String activateOrLogin(String collegeCode,
                                  String regId,
                                  String email,
                                  String password) {

        // 🔐 Step 1: Validate Master List (Strict Mode)
        StudentMasterList master = masterRepository
                .findByCollegeCodeAndRegId(collegeCode, regId)
                .orElseThrow(() -> new RuntimeException("Invalid Reg ID or College Code"));

        if (!master.getOfficialEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Email does not match official records");
        }

        // 🔎 Step 2: Check if student already activated
        Optional<Student> existingStudent =
                studentRepository.findByCollegeCodeAndRegId(collegeCode, regId);

        // 🟢 First-Time Activation
        if (existingStudent.isEmpty()) {

            Student student = new Student();
            student.setCollegeCode(collegeCode);
            student.setRegId(regId);
            student.setFullName(master.getFullName());
            student.setEmail(email);
            student.setPassword(passwordEncoder.encode(password));
            student.setSemester(master.getSemester());
            student.setSection(master.getSection());
            student.setProfileCompleted(false);

            studentRepository.save(student);

            // Mark master as registered
            master.setRegistered(true);
            masterRepository.save(master);

            return "Account Activated Successfully";
        }

        // 🔵 Normal Login
        Student student = existingStudent.get();

        if (!passwordEncoder.matches(password, student.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        return "Login Successful";
    }


    public String googleActivateOrLogin(String collegeCode,
                                        String regId,
                                        String email,
                                        String googleId) {

        // Step 1: Validate master list
        StudentMasterList master = masterRepository
                .findByCollegeCodeAndRegId(collegeCode, regId)
                .orElseThrow(() ->
                        new RuntimeException("Invalid Reg ID"));

        if (!master.getOfficialEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Email does not match official records");
        }

        // Step 2: Check if student already exists
        Optional<Student> existing =
                studentRepository.findByCollegeCodeAndRegId(collegeCode, regId);

        if (existing.isEmpty()) {

            Student student = new Student();
            student.setCollegeCode(collegeCode);
            student.setRegId(regId);
            student.setFullName(master.getFullName());
            student.setEmail(email);
            student.setGoogleId(googleId);
            student.setSemester(master.getSemester());
            student.setSection(master.getSection());

            studentRepository.save(student);

            master.setRegistered(true);
            masterRepository.save(master);

            return "Google Account Activated";
        }

        return "Google Login Successful";
    }


    // ==================================
    // Send Forgot Password OTP
    // ==================================
    public void sendForgotPasswordOtp(String email){

        Student student = studentRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String otp = String.valueOf((int)(Math.random()*900000)+100000);

        otpStore.put(email,otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        try{

            Map<String,String> values = new HashMap<>();
            values.put("NAME", student.getFullName());
            values.put("OTP", otp);

            emailService.sendEmailFromTemplate(
                    "student-password-reset.html",
                    email,
                    values
            );

            emailService.logPasswordResetOtp(email,otp);

        }catch(Exception e){
            throw new RuntimeException("Failed to send OTP");
        }
    }


    // ==================================
    // Verify OTP
    // ==================================
    public void verifyOtp(String email,String otp){

        if(!otpStore.containsKey(email)){
            throw new RuntimeException("OTP not requested");
        }

        if(otpExpiry.get(email).isBefore(LocalDateTime.now())){
            otpStore.remove(email);
            throw new RuntimeException("OTP expired");
        }

        if(!otpStore.get(email).equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }
    }


    // ==================================
    // Reset Password
    // ==================================
    public void resetPassword(String email,String newPassword){

        Student student = studentRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setPassword(passwordEncoder.encode(newPassword));

        studentRepository.save(student);

        otpStore.remove(email);
        otpExpiry.remove(email);

        emailService.logPasswordResetSuccess(email);
    }
}