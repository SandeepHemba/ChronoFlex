package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.FacultyAuthRequest;
import com.example.ChronoFlex.dto.FacultyTimetableOverviewDTO;
import com.example.ChronoFlex.service.AllFacultyTimeTableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/timetable")
@CrossOrigin(origins = "*")
public class AllFacultyTimeTableController {

    private final AllFacultyTimeTableService allFacultyTimeTableService;

    public AllFacultyTimeTableController(AllFacultyTimeTableService allFacultyTimeTableService) {
        this.allFacultyTimeTableService = allFacultyTimeTableService;
    }

    // ✅ Endpoint: /api/faculty/timetable/all
    // Requires only admin credentials
    @PostMapping("/all")
    public ResponseEntity<?> getAllFacultyTimetables(@RequestBody AdminAuthRequest request) {
        try {
            List<FacultyTimetableOverviewDTO> overview = allFacultyTimeTableService.getAllFacultyTimetables(
                    request.getAdminEmail(),
                    request.getAdminPassword()
            );

            // Force serialize to string first — if this throws, you'll see the real error
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(overview); // ← will throw if serialization fails

            return ResponseEntity.ok(overview);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Unauthorized", e.getMessage())
            );
        } catch (Exception e) {
            e.printStackTrace(); // ← now you'll see the real error in console
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error", e.getMessage())
            );
        }
    }

    // ✅ Request DTO
    public static class AdminAuthRequest {
        private String adminEmail;
        private String adminPassword;

        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    }

    // ✅ Error Response DTO
    public static class ErrorResponse {
        private String status;
        private String message;

        public ErrorResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }


    // ✅ Endpoint: /api/faculty/timetable/me
    @PostMapping("/me")
    public ResponseEntity<?> getMyTimetable(@RequestBody FacultyAuthRequest request) {
        try {
            FacultyTimetableOverviewDTO timetable =
                    allFacultyTimeTableService.getFacultyTimetable(
                            request.getEmail(),
                            request.getPassword()
                    );

            return ResponseEntity.ok(timetable);

        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Unauthorized", e.getMessage())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error", e.getMessage())
            );
        }
    }
}
