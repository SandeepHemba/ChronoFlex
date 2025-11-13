package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.TemplateRequest;
import com.example.ChronoFlex.model.Template;
import com.example.ChronoFlex.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    // --------------------------
    // Add Template
    // --------------------------
    @PostMapping("/add")
    public ResponseEntity<?> addTemplate(@RequestBody TemplateRequest request) {
        try {
            Template template = templateService.addTemplate(request);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------
    // Fetch Templates
    // --------------------------
    @PostMapping("/fetch-all")
    public ResponseEntity<?> fetchTemplates(@RequestBody Map<String, String> request) {
        try {
            List<Template> templates = templateService.fetchTemplates(
                    request.get("adminEmail"),
                    request.get("adminPassword")
            );
            if (templates.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No templates found"));
            }
            return ResponseEntity.ok(templates);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------
    // Delete Template
    // --------------------------
    @DeleteMapping("/delete/{templateId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long templateId,
                                            @RequestBody Map<String, String> request) {
        try {
            String result = templateService.deleteTemplate(
                    templateId,
                    request.get("adminEmail"),
                    request.get("adminPassword")
            );
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------
    // Edit Template
    // --------------------------
    @PutMapping("/edit/{templateId}")
    public ResponseEntity<?> editTemplate(@PathVariable Long templateId,
                                          @RequestBody TemplateRequest request) {
        try {
            Template updatedTemplate = templateService.editTemplate(templateId, request);
            return ResponseEntity.ok(updatedTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
