package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.TemplateRequest;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Template;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import com.example.ChronoFlex.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // --------------------------
    // Add Template
    // --------------------------
    public Template addTemplate(TemplateRequest request) {
        Admin admin = adminService.verifyAdmin(request.getAdminEmail(), request.getAdminPassword());
        String collegeCode = admin.getCollegeCode();

        College college = collegeRepository.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new RuntimeException("College not found for code: " + collegeCode));

        Template template = new Template();
        template.setCollege(college);
        template.setTemplateName(request.getTemplateName());
        template.setWorkingDays(request.getWorkingDays());
        template.setClassesPerDay(request.getClassesPerDay());
        template.setDurationPerClass(request.getDurationPerClass());
        template.setNumBreaks(request.getNumBreaks());
        template.setDurationOfBreak(request.getDurationOfBreak());
        template.setBreakTimeFrom(request.getBreakTimeFrom());
        template.setBreakTimeTo(request.getBreakTimeTo());
        template.setStartTime(request.getStartTime());
        template.setEndTime(request.getEndTime());
        template.setMaxHoursPerFacultyPerDay(request.getMaxHoursPerFacultyPerDay());
        template.setMaxHoursPerFacultyPerWeek(request.getMaxHoursPerFacultyPerWeek());
        template.setCreatedBy(admin);
        template.setCreatedAt(LocalDateTime.now());

        return templateRepository.save(template);
    }

    // --------------------------
    // Fetch Templates
    // --------------------------
    public List<Template> fetchTemplates(String adminEmail, String adminPassword) {
        Admin admin = adminService.verifyAdmin(adminEmail, adminPassword);
        String collegeCode = admin.getCollegeCode();

        College college = collegeRepository.findByCollegeCode(collegeCode)
                .orElseThrow(() -> new RuntimeException("College not found for code: " + collegeCode));

        return templateRepository.findByCollege(college);
    }

    // --------------------------
    // Delete Template
    // --------------------------
    public String deleteTemplate(Long templateId, String adminEmail, String adminPassword) {
        adminService.verifyAdmin(adminEmail, adminPassword);

        Template template = templateRepository.findById(Math.toIntExact(templateId))
                .orElseThrow(() -> new RuntimeException("Template not found"));

        templateRepository.delete(template);
        return "Template deleted successfully";
    }

    // --------------------------
    // Edit Template
    // --------------------------
    public Template editTemplate(Long templateId, TemplateRequest request) {
        Admin admin = adminService.verifyAdmin(request.getAdminEmail(), request.getAdminPassword());

        Template template = templateRepository.findById(Math.toIntExact(templateId))
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (request.getTemplateName() != null) template.setTemplateName(request.getTemplateName());
        if (request.getWorkingDays() != null) template.setWorkingDays(request.getWorkingDays());
        if (request.getClassesPerDay() != null) template.setClassesPerDay(request.getClassesPerDay());
        if (request.getDurationPerClass() != null) template.setDurationPerClass(request.getDurationPerClass());
        if (request.getNumBreaks() != null) template.setNumBreaks(request.getNumBreaks());
        if (request.getDurationOfBreak() != null) template.setDurationOfBreak(request.getDurationOfBreak());
        if (request.getBreakTimeFrom() != null) template.setBreakTimeFrom(request.getBreakTimeFrom());
        if (request.getBreakTimeTo() != null) template.setBreakTimeTo(request.getBreakTimeTo());
        if (request.getStartTime() != null) template.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) template.setEndTime(request.getEndTime());
        if (request.getMaxHoursPerFacultyPerDay() != null)
            template.setMaxHoursPerFacultyPerDay(request.getMaxHoursPerFacultyPerDay());
        if (request.getMaxHoursPerFacultyPerWeek() != null)
            template.setMaxHoursPerFacultyPerWeek(request.getMaxHoursPerFacultyPerWeek());

        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }
}
