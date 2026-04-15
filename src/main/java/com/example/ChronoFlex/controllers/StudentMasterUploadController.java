package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.StudentMasterList;
import com.example.ChronoFlex.service.AdminService;
import com.example.ChronoFlex.service.StudentMasterUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
public class StudentMasterUploadController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private StudentMasterUploadService uploadService;

    @PostMapping("/upload-students")
    public ResponseEntity<?> uploadStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid Excel file");
        }

        try {
            // 🔐 Verify admin every time
            Admin admin = adminService.verifyAdmin(email, password);

            String result = uploadService.uploadStudents(file, admin);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<?> fetchStudents(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String section
    ) {

        try {
            // 🔐 Verify admin
            Admin admin = adminService.verifyAdmin(email, password);

            var students = uploadService.fetchStudents(
                    admin.getCollegeCode(), semester, section);

            return ResponseEntity.ok(students);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/students/{regId}")
    public ResponseEntity<?> updateStudent(
            @PathVariable String regId,
            @RequestParam String email,
            @RequestParam String password,
            @RequestBody StudentMasterList updatedData
    ) {

        try {
            Admin admin = adminService.verifyAdmin(email, password);

            String result = uploadService.updateStudent(
                    admin.getCollegeCode(), regId, updatedData);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }
}