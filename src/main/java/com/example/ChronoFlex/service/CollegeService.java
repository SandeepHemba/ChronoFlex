package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.College;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.ChronoFlex.repository.CollegeRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CollegeService {

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private EmailService emailService;

    // Temporary storage for OTP verification
    private Map<String, College> pendingColleges = new ConcurrentHashMap<>();
    private Map<String, String> otpMap = new ConcurrentHashMap<>();

    public void initiateRegistration(College college) throws IOException {
        // Check if college code already exists in DB
        if (collegeRepository.findByCollegeCode(college.getCollegeCode()).isPresent()) {
            throw new IllegalArgumentException("College already registered with this code");
        }

        // Check if college code is already pending OTP
        if (pendingColleges.containsKey(college.getCollegeCode())) {
            throw new IllegalArgumentException("OTP already sent for this college code. Verify OTP to complete registration.");
        }

        // Generate OTP and store temporarily
        String otp = generateOTP();
        pendingColleges.put(college.getCollegeCode(), college);
        otpMap.put(college.getCollegeCode(), otp);

        // Send OTP email
        Map<String, String> values = Map.of(
                "Name", college.getCollegeName(),
                "OTP", otp
        );
        emailService.sendEmailFromTemplate("college_registration_otp.txt",
                college.getContactEmail(), values);
    }

    public boolean verifyOtpAndRegister(String collegeCode, String otp) {
        String savedOtp = otpMap.get(collegeCode);
        if (savedOtp != null && savedOtp.equals(otp)) {
            College college = pendingColleges.get(collegeCode);
            collegeRepository.save(college); // save in DB only after OTP verification
            // remove from temporary storage
            pendingColleges.remove(collegeCode);
            otpMap.remove(collegeCode);
            return true;
        }
        return false;
    }

    private String generateOTP() {
        int otp = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public College getCollegeByCode(String collegeCode) {
        return collegeRepository.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new RuntimeException("College not found with code: " + collegeCode));
    }
}
