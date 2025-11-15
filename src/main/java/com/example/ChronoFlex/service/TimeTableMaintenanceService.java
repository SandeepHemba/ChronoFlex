package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeTableMaintenanceService {

    private final AdminRepository adminRepo;
    private final CollegeRepository collegeRepo;
    private final CollegeClassRepository classRepo;
    private final com.example.chronoflex.repository.FacultyAvailabilityRepository facultyAvailabilityRepo;
    private final TimeTableBackupRepository backupRepo;
    private final TimeTableAuditLogRepository auditLogRepo;
    private final EmailService emailService; // existing email logger + sender
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TimeTableMaintenanceService(AdminRepository adminRepo,
                                       CollegeRepository collegeRepo,
                                       CollegeClassRepository classRepo,
                                       com.example.chronoflex.repository.FacultyAvailabilityRepository facultyAvailabilityRepo,
                                       TimeTableBackupRepository backupRepo,
                                       TimeTableAuditLogRepository auditLogRepo,
                                       EmailService emailService) {
        this.adminRepo = adminRepo;
        this.collegeRepo = collegeRepo;
        this.classRepo = classRepo;
        this.facultyAvailabilityRepo = facultyAvailabilityRepo;
        this.backupRepo = backupRepo;
        this.auditLogRepo = auditLogRepo;
        this.emailService = emailService;
    }

    private String toRoman(String semesterNumber) {
        switch (semesterNumber) {
            case "1": return "I";
            case "2": return "II";
            case "3": return "III";
            case "4": return "IV";
            case "5": return "V";
            case "6": return "VI";
            default: return semesterNumber;
        }
    }

    /**
     * Backup all availability rows for the class (semester+section) belonging to the admin's college,
     * then delete them (i.e. make FREE / remove BUSY rows).
     *
     * Returns message about how many slots were backed up & deleted.
     */
    public String backupAndDeleteTimetable(String adminEmail,
                                           String adminPassword,
                                           String semesterNumber,
                                           String section) throws Exception {

        // 1) Authenticate admin
        Admin admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalAccessException("Invalid admin credentials"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new IllegalAccessException("Invalid admin credentials");
        }
        if (!admin.isVerified()) {
            throw new IllegalAccessException("Admin account not verified");
        }

        // 2) Resolve college from admin
        College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("College not found for admin"));

        Long collegeId = college.getCollegeId();

        // 3) Determine class (classId) from collegeId + semester + section
        String semesterRoman = toRoman(semesterNumber);
        CollegeClass collegeClass = classRepo
                .findByCollege_CollegeIdAndSemesterAndSection(collegeId, semesterRoman, section)
                .orElseThrow(() -> new IllegalStateException("Class not found for given semester & section"));

        Integer classId = collegeClass.getClassId();

        // 4) Fetch existing availability rows for that class
        List<FacultyAvailability> allForClass = facultyAvailabilityRepo.findByClassId(classId);
        // filter by semester & section (some APIs may store these fields; be safe)
        List<FacultyAvailability> toBackup = allForClass.stream()
                .filter(fa -> {
                    if (fa.getSemester() == null && fa.getSection() == null) return false;
                    String faSem = fa.getSemester() == null ? "" : fa.getSemester();
                    String faSec = fa.getSection() == null ? "" : fa.getSection();
                    return faSem.equalsIgnoreCase(semesterRoman) && faSec.equalsIgnoreCase(section);
                })
                .collect(Collectors.toList());

        if (toBackup.isEmpty()) {
            return "No timetable slots found for " + semesterRoman + " - " + section;
        }

        // 5) Create backups
        LocalDateTime now = LocalDateTime.now();
        List<TimeTableBackup> backups = new ArrayList<>();

        for (FacultyAvailability fa : toBackup) {
            TimeTableBackup tb = new TimeTableBackup(
                    fa.getAvailabilityId(),
                    fa.getFacultyId(),
                    fa.getDayOfWeek(),
                    fa.getStartTime(),
                    fa.getEndTime(),
                    fa.getClassId(),
                    fa.getSubjectId(),
                    fa.getSemester(),
                    fa.getSection(),
                    fa.getTemplateId(),
                    fa.getStatus(),
                    fa.getCollegeId(),
                    fa.getCreatedBy(),
                    fa.getCreatedAt(),
                    fa.getUpdatedAt(),
                    admin.getAdminId(),
                    now
            );
            backups.add(tb);
        }

        backupRepo.saveAll(backups);

        // 6) Delete the original availability rows
        // We do not call any custom delete query (to avoid modifying existing repo),
        // we simply delete the entities we fetched.
        facultyAvailabilityRepo.deleteAll(toBackup);

        // 7) Audit log
        String message = String.format("Admin '%s' (%d) backed up & deleted %d slots for %s-%s",
                admin.getName(), admin.getAdminId(), backups.size(), semesterRoman, section);

        TimeTableAuditLog audit = new TimeTableAuditLog(
                admin.getAdminId(),
                collegeId,
                null,
                "DELETE_TIMETABLE",
                semesterRoman,
                section,
                "success",
                message
        );
        audit.setGeneratedAt(now);
        auditLogRepo.save(audit);

        // 8) Email notification to admin (uses your existing EmailService template loader)
        try {
            Map<String, String> values = Map.of(
                    "ADMIN_NAME", admin.getName(),
                    "SEMESTER", semesterRoman,
                    "SECTION", section,
                    "COLLEGE", college.getCollegeName(),
                    "SLOTS_COUNT", String.valueOf(backups.size()),
                    "TIMESTAMP", now.toString()
            );

            // template "timetable_deleted_notification.html" assumed present in resources/templates/email/
            emailService.sendEmailFromTemplate("timetable_deleted_notification.html", admin.getEmail(), values);

            // Also log email activity
            emailService.logEmailActivity(admin.getEmail(), "Timetable Deleted", message);

        } catch (Exception e) {
            // log the failure into email logs and continue
            emailService.logEmailActivity(admin.getEmail(), "Timetable Deleted - Email Failed", e.getMessage());
        }

        return "Deleted and backed up " + backups.size() + " slots for " + semesterRoman + " - " + section;
    }

    /**
     * Fetch delete history (backups) for admin's college for given semester+section.
     */
    public List<TimeTableBackup> getDeleteHistory(String adminEmail,
                                                  String adminPassword,
                                                  String semesterNumber,
                                                  String section) throws Exception {

        // Authenticate admin (same as above)
        Admin admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalAccessException("Invalid admin credentials"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new IllegalAccessException("Invalid admin credentials");
        }
        if (!admin.isVerified()) {
            throw new IllegalAccessException("Admin account not verified");
        }

        College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("College not found for admin"));

        Long collegeId = college.getCollegeId();
        String semesterRoman = toRoman(semesterNumber);

        return backupRepo.findByCollegeIdAndSemesterAndSectionOrderByBackedUpAtDesc(collegeId, semesterRoman, section);
    }

}
