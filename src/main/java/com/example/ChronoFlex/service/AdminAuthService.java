package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    @Autowired
    private AdminRepository adminRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Authenticate admin using email and password
    public Admin authenticateAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid admin email"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid admin password");
        }

        if (!admin.isVerified()) {
            throw new RuntimeException("Admin is not verified");
        }

        return admin;
    }
}
