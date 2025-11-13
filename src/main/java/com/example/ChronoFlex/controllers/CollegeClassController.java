package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.CollegeClassRequest;
import com.example.ChronoFlex.model.CollegeClass;
import com.example.ChronoFlex.service.CollegeClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
public class CollegeClassController {

    @Autowired
    private CollegeClassService classService;

    // --------------------------
    // Add new class
    // --------------------------
    @PostMapping("/add")
    public ResponseEntity<?> addClass(@RequestBody CollegeClassRequest request) {
        try {
            CollegeClass collegeClass = classService.addClass(request);
            return ResponseEntity.ok(collegeClass);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --------------------------
    // Fetch classes
    // --------------------------
    @PostMapping("/fetch-all")
    public ResponseEntity<?> fetchClasses(@RequestBody Map<String, Object> request) {
        try {
            String statusStr = (String) request.get("status");
            CollegeClass.Status status = null;
            if (statusStr != null && !statusStr.isEmpty()) {
                status = CollegeClass.Status.valueOf(statusStr.toUpperCase());
            }

            List<CollegeClass> classes = classService.fetchClasses(
                    (String) request.get("adminEmail"),
                    (String) request.get("adminPassword"),
                    (String) request.get("collegeCode"),
                    (String) request.get("semester"),
                    status
            );

            if (classes.isEmpty()) {
                return ResponseEntity.ok("No classes found for the given filters.");
            }

            return ResponseEntity.ok(classes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value");
        }
    }

    // --------------------------
    // Edit class
    // --------------------------
    @PutMapping("/edit/{classId}")
    public ResponseEntity<?> editClass(@PathVariable Integer classId, @RequestBody CollegeClassRequest request) {
        try {
            CollegeClass updatedClass = classService.editClass(classId, request);
            return ResponseEntity.ok(updatedClass);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --------------------------
    // Delete class
    // --------------------------
    @DeleteMapping("/delete/{classId}")
    public ResponseEntity<?> deleteClass(@PathVariable Integer classId,
                                         @RequestParam String adminEmail,
                                         @RequestParam String adminPassword) {
        try {
            String msg = classService.deleteClass(classId, adminEmail, adminPassword);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
