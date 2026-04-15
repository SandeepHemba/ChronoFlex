package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.TimetableSlotDTO;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import com.example.ChronoFlex.service.TimeTableViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/timetable")
@CrossOrigin(origins = "*")
public class TimeTableViewController {

    private final TimeTableViewService timeTableViewService;
    private final AdminRepository adminRepo;
    private final CollegeRepository collegeRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TimeTableViewController(TimeTableViewService timeTableViewService,
                                   AdminRepository adminRepo,
                                   CollegeRepository collegeRepo) {
        this.timeTableViewService = timeTableViewService;
        this.adminRepo = adminRepo;
        this.collegeRepo = collegeRepo;
    }

    @PostMapping("/view")
    public ResponseEntity<?> getTimeTable(@RequestBody TimetableRequest request) {
        try {
            if (request.getAdminEmail() == null || request.getAdminPassword() == null)
                return ResponseEntity.status(401).body("Admin credentials required.");

            Admin admin = adminRepo.findByEmail(request.getAdminEmail())
                    .orElseThrow(() -> new IllegalAccessException("Invalid admin email or password."));

            if (!passwordEncoder.matches(request.getAdminPassword(), admin.getPassword()))
                throw new IllegalAccessException("Invalid admin credentials.");

            if (!admin.isVerified())
                throw new IllegalAccessException("Admin account not verified.");

            College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                    .orElseThrow(() -> new IllegalStateException("College not found for admin."));

            List<TimetableSlotDTO> slots = timeTableViewService.getTimeTable(
                    college.getCollegeId(),
                    request.getSemester(),
                    request.getSection()
            );

            if (slots.isEmpty())
                return ResponseEntity.status(404).body("No timetable found for the given semester and section.");

            return ResponseEntity.ok(slots);

        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public static class TimetableRequest {
        private String adminEmail;
        private String adminPassword;
        private String semester;
        private String section;

        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
    }
}