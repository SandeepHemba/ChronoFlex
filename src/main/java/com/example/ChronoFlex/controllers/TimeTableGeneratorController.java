package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.TimeTableGenerationRequest;
import com.example.ChronoFlex.model.TimeTableAuditLog;
import com.example.ChronoFlex.service.TimeTableGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
public class TimeTableGeneratorController {

    private final TimeTableGeneratorService generatorService;

    @Autowired
    public TimeTableGeneratorController(TimeTableGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    /**
     * ✅ Generate timetable using a template + optional rule filters
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateTimeTable(@RequestBody TimeTableGenerationRequest request) {
        try {
            String result = generatorService.generateTimeTable(
                    request.getCollegeId(),
                    request.getSemester(),
                    request.getSection(),
                    request.getTemplateId(),
                    request.getAdminId(),
                    request.getFilters()
            );
            return ResponseEntity.ok(result);
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

}
