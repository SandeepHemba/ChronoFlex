package com.example.ChronoFlex.controller;

import com.example.ChronoFlex.dto.FacultyAccessResponseDTO;
import com.example.ChronoFlex.service.FacultyAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyAccessController {

    @Autowired
    private FacultyAccessService facultyAccessService;

    @PostMapping("/access")
    public ResponseEntity<?> getFacultyAccess(@RequestBody Map<String, String> request) {

        try {
            String email = request.get("email");
            String password = request.get("password");

            FacultyAccessResponseDTO response =
                    facultyAccessService.getFacultyAccess(email, password);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}