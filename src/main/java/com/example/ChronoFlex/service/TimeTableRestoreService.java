package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.RestoreDTOs.*;
import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to list/preview/restore timetable backups.
 * - Uses detailed audit logs (Option 1)
 * - Expects TimeTableBackup model to expose getId(), getBackupByAdminId(), getBackedUpAt()
 */
@Service
public class TimeTableRestoreService {

    private final AdminRepository adminRepo;
    private final CollegeRepository collegeRepo;
    private final CollegeClassRepository classRepo;
    private final FacultyRepository facultyRepo;
    private final SubjectRepository subjectRepo;
    private final TimeTableBackupRepository backupRepo;

    // repository in different package (your project)
    private final com.example.chronoflex.repository.FacultyAvailabilityRepository availabilityRepo;

    private final TimeTableAuditLogRepository auditLogRepo;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TimeTableRestoreService(
            AdminRepository adminRepo,
            CollegeRepository collegeRepo,
            CollegeClassRepository classRepo,
            FacultyRepository facultyRepo,
            SubjectRepository subjectRepo,
            TimeTableBackupRepository backupRepo,
            com.example.chronoflex.repository.FacultyAvailabilityRepository availabilityRepo,
            TimeTableAuditLogRepository auditLogRepo,
            EmailService emailService
    ) {
        this.adminRepo = adminRepo;
        this.collegeRepo = collegeRepo;
        this.classRepo = classRepo;
        this.facultyRepo = facultyRepo;
        this.subjectRepo = subjectRepo;
        this.backupRepo = backupRepo;
        this.availabilityRepo = availabilityRepo;
        this.auditLogRepo = auditLogRepo;
        this.emailService = emailService;
    }

    // ===================================================================
    // LIST BACKUPS
    // ===================================================================
    public List<BackupSummary> listBackups(
            String adminEmail,
            String adminPassword,
            String semesterNumber,
            String section
    ) throws Exception {

        Admin admin = authenticateAdmin(adminEmail, adminPassword);

        College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("College not found"));

        String semesterRoman = toRoman(semesterNumber);

        List<TimeTableBackup> backups =
                backupRepo.findByCollegeIdAndSemesterAndSectionOrderByBackedUpAtDesc(
                        college.getCollegeId(), semesterRoman, section
                );

        // Map to BackupSummary DTO
        List<BackupSummary> summaries = backups.stream().map(b -> {
            BackupSummary s = new BackupSummary();
            s.setBackupId(b.getId());
            s.setBackedUpAt(b.getBackedUpAt());
            s.setAdminId(b.getBackupByAdminId());
            s.setCollegeId(b.getCollegeId());
            s.setSemester(b.getSemester());
            s.setSection(b.getSection());
            s.setSlotsCount(1); // if you store one row per slot; consider grouping later
            return s;
        }).collect(Collectors.toList());

        // Audit: list action
        String message = String.format("Admin '%s' listed backups for %s-%s (found %d rows)",
                admin.getName(), semesterRoman, section, summaries.size());
        logAudit(admin.getAdminId(), college.getCollegeId(), null, "LIST_BACKUPS", semesterRoman, section, "info", message);

        return summaries;
    }

    // ===================================================================
    // PREVIEW RESTORE (conflict check)
    // ===================================================================
    public PreviewResponse previewRestore(String adminEmail, String adminPassword, Long backupId) throws Exception {

        Admin admin = authenticateAdmin(adminEmail, adminPassword);

        TimeTableBackup exemplar = backupRepo.findById(backupId)
                .orElseThrow(() -> new IllegalArgumentException("Backup not found"));

        // Group by same backedUpAt + backupByAdminId + semester + section
        List<TimeTableBackup> group =
                backupRepo.findByCollegeIdAndSemesterAndSectionOrderByBackedUpAtDesc(
                                exemplar.getCollegeId(), exemplar.getSemester(), exemplar.getSection()
                        ).stream()
                        .filter(b -> Objects.equals(b.getBackedUpAt(), exemplar.getBackedUpAt())
                                && Objects.equals(b.getBackupByAdminId(), exemplar.getBackupByAdminId()))
                        .collect(Collectors.toList());

        if (group.isEmpty()) group = List.of(exemplar);

        CollegeClass collegeClass =
                classRepo.findByCollege_CollegeIdAndSemesterAndSection(
                        exemplar.getCollegeId(), exemplar.getSemester(), exemplar.getSection()
                ).orElseThrow(() -> new IllegalStateException("Class not found"));

        Integer classId = collegeClass.getClassId();

        List<ConflictDetail> conflicts = new ArrayList<>();

        for (TimeTableBackup tb : group) {
            ConflictDetail cd = checkSlotConflict(tb, classId);
            if (cd != null) conflicts.add(cd);
        }

        PreviewResponse resp = new PreviewResponse();
        resp.setBackupId(backupId);
        resp.setCollegeId(exemplar.getCollegeId());
        resp.setSemester(exemplar.getSemester());
        resp.setSection(exemplar.getSection());
        resp.setTotalSlots(group.size());
        resp.setConflictCount(conflicts.size());
        resp.setConflicts(conflicts);

        // Audit: preview action
        String msg = String.format("Admin '%s' previewed restore for backupId=%d (%d slots, %d conflicts)",
                admin.getName(), backupId, group.size(), conflicts.size());
        logAudit(admin.getAdminId(), exemplar.getCollegeId(), null, "PREVIEW_RESTORE", exemplar.getSemester(), exemplar.getSection(), (conflicts.isEmpty() ? "info" : "warning"), msg);

        return resp;
    }

    // ===================================================================
    // RESTORE BACKUP (strict mode: block on any conflict)
    // ===================================================================
    @Transactional
    public RestoreResult restoreBackup(String adminEmail, String adminPassword, Long backupId) throws Exception {

        Admin admin = authenticateAdmin(adminEmail, adminPassword);

        TimeTableBackup exemplar = backupRepo.findById(backupId)
                .orElseThrow(() -> new IllegalArgumentException("Backup not found"));

        List<TimeTableBackup> group =
                backupRepo.findByCollegeIdAndSemesterAndSectionOrderByBackedUpAtDesc(
                                exemplar.getCollegeId(), exemplar.getSemester(), exemplar.getSection()
                        ).stream()
                        .filter(b -> Objects.equals(b.getBackedUpAt(), exemplar.getBackedUpAt())
                                && Objects.equals(b.getBackupByAdminId(), exemplar.getBackupByAdminId()))
                        .collect(Collectors.toList());

        if (group.isEmpty()) group = List.of(exemplar);

        CollegeClass collegeClass =
                classRepo.findByCollege_CollegeIdAndSemesterAndSection(
                        exemplar.getCollegeId(), exemplar.getSemester(), exemplar.getSection()
                ).orElseThrow(() -> new IllegalStateException("Class not found"));

        Integer classId = collegeClass.getClassId();

        List<ConflictDetail> conflicts = new ArrayList<>();
        for (TimeTableBackup tb : group) {
            ConflictDetail cd = checkSlotConflict(tb, classId);
            if (cd != null) conflicts.add(cd);
        }

        RestoreResult result = new RestoreResult();

        if (!conflicts.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Restore blocked due to conflicts.");
            result.setConflicts(conflicts);
            result.setRestoredSlots(0);
            result.setSkippedSlots(group.size());

            String message = String.format("Restore blocked for admin '%s' — %d conflicts (backup %s %s %s)",
                    admin.getName(), conflicts.size(), exemplar.getSemester(), exemplar.getSection(), exemplar.getBackedUpAt());
            logAudit(admin.getAdminId(), exemplar.getCollegeId(), null, "RESTORE_ATTEMPT_BLOCKED", exemplar.getSemester(), exemplar.getSection(), "failed", message);
            return result;
        }

        // No conflicts: proceed
        // 1) delete current timetable rows for the class (by classId + semester + section)
        availabilityRepo.deleteByClassIdAndSemesterAndSection(classId, exemplar.getSemester(), exemplar.getSection());

        // 2) Restore all backup rows
        List<FacultyAvailability> toSave = new ArrayList<>();
        for (TimeTableBackup tb : group) {
            FacultyAvailability fa = new FacultyAvailability();
            fa.setFacultyId(tb.getFacultyId());
            fa.setDayOfWeek(tb.getDayOfWeek());
            fa.setStartTime(tb.getStartTime());
            fa.setEndTime(tb.getEndTime());
            fa.setClassId(tb.getClassId());
            fa.setSubjectId(tb.getSubjectId());
            fa.setSemester(tb.getSemester());
            fa.setSection(tb.getSection());
            fa.setTemplateId(tb.getTemplateId());
            fa.setStatus(FacultyAvailability.Status.BUSY);
            fa.setCollegeId(tb.getCollegeId());
            fa.setCreatedBy(admin.getAdminId());
            toSave.add(fa);
        }

        availabilityRepo.saveAll(toSave);

        // Audit successful restore
        String successMsg = String.format("Admin '%s' restored %d slots for %s-%s (backup at %s)",
                admin.getName(), toSave.size(), exemplar.getSemester(), exemplar.getSection(), exemplar.getBackedUpAt());
        logAudit(admin.getAdminId(), exemplar.getCollegeId(), null, "RESTORE", exemplar.getSemester(), exemplar.getSection(), "success", successMsg);

        // Prepare HTML rows to include in email template
        StringBuilder rows = new StringBuilder();
        rows.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%;font-family:Arial;'>");
        rows.append("<thead><tr><th>Day</th><th>Time</th><th>Subject</th><th>Faculty</th></tr></thead><tbody>");
        for (FacultyAvailability fa : toSave) {
            Optional<Faculty> fact = facultyRepo.findById(fa.getFacultyId());
            Optional<Subject> subj = fa.getSubjectId() == null ? Optional.empty() : subjectRepo.findById(fa.getSubjectId());
            String facultyName = fact.map(Faculty::getName).orElse("Unknown");
            String subjectName = subj.map(Subject::getSubjectName).orElse("Unknown");

            rows.append("<tr>")
                    .append("<td>").append(fa.getDayOfWeek() != null ? fa.getDayOfWeek().name() : "N/A").append("</td>")
                    .append("<td>").append(fa.getStartTime()).append(" - ").append(fa.getEndTime()).append("</td>")
                    .append("<td>").append(subjectName).append("</td>")
                    .append("<td>").append(facultyName).append("</td>")
                    .append("</tr>");
        }
        rows.append("</tbody></table>");

        // Send email notification to admin
        Map<String, String> values = new HashMap<>();
        values.put("ADMIN_NAME", admin.getName());
        values.put("SEMESTER", exemplar.getSemester());
        values.put("SECTION", exemplar.getSection());
        values.put("COLLEGE", collegeRepo.findById(exemplar.getCollegeId()).map(College::getCollegeName).orElse("College"));
        values.put("SLOTS_COUNT", String.valueOf(toSave.size()));
        values.put("TIMESTAMP", LocalDateTime.now().toString());
        values.put("TIMETABLE_ROWS", rows.toString());

        try {
            emailService.sendEmailFromTemplate("timetable_restored_notification.html", admin.getEmail(), values);
            emailService.logEmailActivity(admin.getEmail(), "Timetable Restored", successMsg);
        } catch (Exception e) {
            emailService.logEmailActivity(admin.getEmail(), "Timetable Restored - Email Failed", e.getMessage());
        }

        result.setSuccess(true);
        result.setMessage("Timetable restored successfully.");
        result.setRestoredSlots(toSave.size());
        result.setSkippedSlots(0);
        result.setConflicts(Collections.emptyList());

        return result;
    }

    // ===================================================================
    // CONFLICT CHECKING (single slot)
    // ===================================================================
    private ConflictDetail checkSlotConflict(TimeTableBackup tb, Integer classId) {
        if (tb.getFacultyId() == null) return makeConflict(tb, "Faculty ID missing");

        Optional<Faculty> facultyOpt = facultyRepo.findById(tb.getFacultyId());
        if (facultyOpt.isEmpty() || !Boolean.TRUE.equals(facultyOpt.get().getIsActive()))
            return makeConflict(tb, "Faculty removed or inactive");

        if (tb.getSubjectId() != null && subjectRepo.findById(tb.getSubjectId()).isEmpty())
            return makeConflict(tb, "Subject removed from current semester");

        List<FacultyAvailability> facultyConflicts =
                availabilityRepo.findFacultyConflicts(tb.getFacultyId(), tb.getDayOfWeek(), tb.getStartTime(), tb.getEndTime());

        if (facultyConflicts != null && !facultyConflicts.isEmpty()) {
            ConflictDetail cd = makeConflict(tb, "Faculty already assigned at this time");
            cd.setConflictingAvailabilityId(facultyConflicts.get(0).getAvailabilityId());
            return cd;
        }

        List<FacultyAvailability> classConflicts =
                availabilityRepo.findClassConflicts(classId, tb.getDayOfWeek(), tb.getStartTime(), tb.getEndTime());

        if (classConflicts != null && !classConflicts.isEmpty()) {
            ConflictDetail cd = makeConflict(tb, "Class already has assignment at this time");
            cd.setConflictingAvailabilityId(classConflicts.get(0).getAvailabilityId());
            return cd;
        }

        return null;
    }

    private ConflictDetail makeConflict(TimeTableBackup tb, String reason) {
        ConflictDetail cd = new ConflictDetail();
        cd.setDay(tb.getDayOfWeek() != null ? tb.getDayOfWeek().name() : "N/A");
        cd.setStartTime(tb.getStartTime() != null ? tb.getStartTime().toString() : "N/A");
        cd.setEndTime(tb.getEndTime() != null ? tb.getEndTime().toString() : "N/A");
        cd.setReason(reason);
        return cd;
    }

    // ===================================================================
    // AUTHENTICATE ADMIN
    // ===================================================================
    private Admin authenticateAdmin(String email, String pass) throws Exception {
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalAccessException("Invalid admin login"));

        if (!passwordEncoder.matches(pass, admin.getPassword()))
            throw new IllegalAccessException("Invalid credentials");

        if (!admin.isVerified())
            throw new IllegalAccessException("Admin not verified");

        return admin;
    }

    // ===================================================================
    // UTILITIES
    // ===================================================================
    private String toRoman(String n) {
        switch (n) {
            case "1": return "I";
            case "2": return "II";
            case "3": return "III";
            case "4": return "IV";
            case "5": return "V";
            case "6": return "VI";
            default: return n;
        }
    }

    /**
     * Detailed audit log writer (Option 1)
     */
    private void logAudit(Long adminId, Long collegeId, Integer templateId,
                          String templateName, String semester, String section,
                          String status, String message) {
        try {
            TimeTableAuditLog log = new TimeTableAuditLog(adminId, collegeId, templateId,
                    templateName, semester, section, status, message);
            log.setGeneratedAt(LocalDateTime.now());
            auditLogRepo.save(log);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to write audit log: " + e.getMessage());
        }
    }
}
