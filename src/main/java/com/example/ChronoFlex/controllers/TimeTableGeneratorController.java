package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.DeleteTimeTableRequest;
import com.example.ChronoFlex.dto.TimeTableGenerationRequest;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.TimeTableAuditLog;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.service.TimeTableGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
public class TimeTableGeneratorController {

    private final TimeTableGeneratorService generatorService;
    private final AdminRepository adminRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public TimeTableGeneratorController(TimeTableGeneratorService generatorService,
                                        AdminRepository adminRepo) {
        this.generatorService = generatorService;
        this.adminRepo = adminRepo;
    }

    /**
     * ✅ Generate timetable using a template + optional rule filters
     * Now authenticates admin using email & password instead of adminId.
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateTimeTable(@RequestBody TimeTableGenerationRequest request) {
        try {
            // 🔐 Step 1: Authenticate admin using email + password
            Admin admin = adminRepo.findByEmail(request.getAdminEmail())
                    .orElseThrow(() -> new IllegalAccessException("Invalid admin email or password."));

            if (!passwordEncoder.matches(request.getAdminPassword(), admin.getPassword())) {
                throw new IllegalAccessException("Invalid admin credentials.");
            }

            if (!admin.isVerified()) {
                throw new IllegalAccessException("Admin account not verified.");
            }

            // ✅ Step 2: Use the verified admin’s ID for the service call
            String result = generatorService.generateTimeTable(
                    request.getCollegeId(),
                    request.getSemester(),
                    request.getSection(),
                    request.getTemplateId(),
                    admin.getAdminId(), // resolved dynamically
                    request.getFilters()
            );

            return ResponseEntity.ok(result);

        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401)
                    .body("❌ Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body("❌ Error generating timetable: " + e.getMessage());
        }
    }

    /**
     * ✅ Free all slots for a given template (used before regenerating)
     * Example: DELETE /api/timetable/free-slots/1
     */
    @DeleteMapping("/free-slots/{templateId}")
    public ResponseEntity<String> freeSlots(@PathVariable Integer templateId) {
        try {
            generatorService.getAvailabilityService().freeSlotsByTemplate(templateId);
            return ResponseEntity.ok("✅ Slots freed successfully for template ID: " + templateId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body("❌ Error freeing slots: " + e.getMessage());
        }
    }


    @PostMapping("/delete")
    public ResponseEntity<?> deleteTimetable(@RequestBody DeleteTimeTableRequest request) {
        try {
            String result = generatorService.deleteTimetableForClass(
                    request.getAdminEmail(),
                    request.getAdminPassword(),
                    request.getSemester(),
                    request.getSection()
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

}
