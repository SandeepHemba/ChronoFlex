package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.FacultySubjectMapRequest;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.FacultySubjectMap;
import com.example.ChronoFlex.repository.*;
import com.example.ChronoFlex.service.FacultySubjectMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty-subject")
@CrossOrigin(origins = "*")
public class FacultySubjectMapController {

    @Autowired
    private FacultySubjectMapService mapService;

    // ============================================================
    // Admin Mapping Endpoint
    // ============================================================
    @PostMapping("/map")
    public ResponseEntity<?> mapFacultyToSubject(@RequestBody FacultySubjectMapRequest request) {
        try {
            FacultySubjectMap map = mapService.mapFacultyToSubject(
                    request.getAdminEmail(),
                    request.getAdminPassword(),
                    request.getFacultyName(),
                    request.getFacultyEmail(),
                    request.getFacultyDob(),
                    request.getSubjectCode(),
                    request.getWeeklyHours(),
                    request.getPreferredDays(),
                    request.getNotes()
            );
            return ResponseEntity.ok(map);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================================
    // Fetch all faculty-subject mappings (Admin only)
    // ============================================================
    @PostMapping("/fetch-all")
    public ResponseEntity<?> fetchAllMappings(@RequestBody FacultySubjectMapRequest request) {
        try {
            List<FacultySubjectMap> mappings = mapService.fetchAllMappings(
                    request.getAdminEmail(),
                    request.getAdminPassword(),
                    request.getDepartment(),  // optional filter
                    request.getSemester(),    // optional filter
                    request.getActive()       // optional filter (true/false)
            );

            if (mappings.isEmpty()) {
                return ResponseEntity.ok("No mappings found for the given filters.");
            }

            return ResponseEntity.ok(mappings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
}
