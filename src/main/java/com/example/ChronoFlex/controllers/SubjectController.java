package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.SubjectRequestDTO;
import com.example.ChronoFlex.model.Subject;
import com.example.ChronoFlex.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subject")
@CrossOrigin(origins = "*")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    // ----------------- Add Subject -----------------
    @PostMapping("/add")
    public ResponseEntity<?> addSubject(@RequestBody SubjectRequestDTO dto) {
        try {
            Subject subject = subjectService.addSubject(dto);
            return ResponseEntity.ok(subject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------- Fetch Subjects -----------------
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchSubjects(@RequestBody SubjectRequestDTO dto) {
        try {
            List<Subject> subjects = subjectService.fetchSubjects(dto);
            if (subjects.isEmpty()) return ResponseEntity.ok("No subjects found");
            return ResponseEntity.ok(subjects);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------- Activate / Deactivate Subject -----------------
    @PostMapping("/toggle-status/{subjectId}")
    public ResponseEntity<?> toggleStatus(
            @PathVariable Long subjectId,
            @RequestParam boolean active,
            @RequestParam String adminEmail,
            @RequestParam String adminPassword
    ) {
        try {
            Subject subject = subjectService.toggleSubjectStatus(subjectId, active, adminEmail, adminPassword);
            return ResponseEntity.ok(subject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
