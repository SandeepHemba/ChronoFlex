package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.TimeTableGenerationFilters;
import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.chronoflex.repository.FacultyAvailabilityRepository;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeTableGeneratorService {

    private final FacultySubjectMapRepository facultySubjectMapRepo;
    private final FacultyAvailabilityService availabilityService;
    private final CollegeClassRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final TemplateRepository templateRepo;
    private final TimeTableAuditLogRepository auditLogRepo; // ✅ Added

    private final AdminRepository adminRepo;
    private final CollegeRepository collegeRepo;
    private final FacultyAvailabilityRepository availabilityRepo;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public TimeTableGeneratorService(
            FacultySubjectMapRepository facultySubjectMapRepo,
            FacultyAvailabilityService availabilityService,
            CollegeClassRepository classRepo,
            SubjectRepository subjectRepo,
            TemplateRepository templateRepo,
            TimeTableAuditLogRepository auditLogRepo, // ✅ Added

            AdminRepository adminRepo,
            CollegeRepository collegeRepo,
            FacultyAvailabilityRepository availabilityRepo,
            EmailService emailService
    ) {
        this.facultySubjectMapRepo = facultySubjectMapRepo;
        this.availabilityService = availabilityService;
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.templateRepo = templateRepo;
        this.auditLogRepo = auditLogRepo; // ✅ Added

        // ⭐ Assign new dependencies
        this.adminRepo = adminRepo;
        this.collegeRepo = collegeRepo;
        this.availabilityRepo = availabilityRepo;
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

    @Transactional
    public String generateTimeTable(Long collegeId,
                                    String semesterNumber,
                                    String section,
                                    Integer templateId,
                                    Long adminId,
                                    TimeTableGenerationFilters filters) {

        String status = "success";
        String message = "";

        try {
            if (filters == null) filters = new TimeTableGenerationFilters();

            Template template = templateRepo.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));

            String semester = toRoman(semesterNumber);

            Optional<CollegeClass> optClass = classRepo.findByCollege_CollegeIdAndSemesterAndSection(
                    collegeId, semester, section
            );
            if (optClass.isEmpty()) {
                message = "❌ No class found for given semester & section!";
                logAudit(adminId, collegeId, templateId, template.getTemplateName(), semester, section, "error", message);
                return message;
            }
            CollegeClass collegeClass = optClass.get();

            List<Subject> subjects = subjectRepo.findByCollege_CollegeIdAndSemesterAndIsActiveTrue(
                    collegeId, semesterNumber
            );
            if (subjects.isEmpty()) {
                message = "❌ No subjects found for this semester!";
                logAudit(adminId, collegeId, templateId, template.getTemplateName(), semester, section, "error", message);
                return message;
            }

            Map<String, String> canonMap = buildCanonicalMap(subjects);

            // Template setup
            List<String> workingDaysList = Arrays.stream(template.getWorkingDays().split(","))
                    .map(s -> s.trim().toUpperCase()).collect(Collectors.toList());
            int slotDuration = template.getDurationPerClass();
            int numBreaks = template.getNumBreaks();
            int durationOfBreak = template.getDurationOfBreak();
            LocalTime startTime = template.getStartTime();
            LocalTime endTime = template.getEndTime();
            LocalTime breakFrom = template.getBreakTimeFrom();
            LocalTime breakTo = template.getBreakTimeTo();

            // Subject ordering: priority first
            List<String> priorityEarlyLower = filters.getPrioritySubjectsEarly() == null
                    ? Collections.emptyList()
                    : filters.getPrioritySubjectsEarly().stream().map(String::toLowerCase).collect(Collectors.toList());

            List<Subject> orderedSubjects = new ArrayList<>(subjects);
            if (!priorityEarlyLower.isEmpty()) {
                orderedSubjects.sort((a, b) -> {
                    boolean aPr = priorityEarlyLower.contains(a.getSubjectName().toLowerCase());
                    boolean bPr = priorityEarlyLower.contains(b.getSubjectName().toLowerCase());
                    return Boolean.compare(!aPr, !bPr);
                });
            } else {
                Collections.shuffle(orderedSubjects);
            }

            // Break configuration
            long totalDayMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            List<LocalTime[]> breaks = new ArrayList<>();
            if (breakFrom != null && breakTo != null) {
                breaks.add(new LocalTime[]{breakFrom, breakTo});
            } else if (numBreaks > 1) {
                long gapBetweenBreaks = totalDayMinutes / (numBreaks + 1);
                LocalTime temp = startTime.plusMinutes(gapBetweenBreaks);
                for (int i = 0; i < numBreaks; i++) {
                    breaks.add(new LocalTime[]{temp, temp.plusMinutes(durationOfBreak)});
                    temp = temp.plusMinutes(gapBetweenBreaks + durationOfBreak);
                }
            }

            // Tracking maps
            Map<String, Integer> perWeekCounts = new HashMap<>();
            Map<String, Map<String, Integer>> perDayCounts = new HashMap<>();
            Map<String, String> lastSubjectPerDay = new HashMap<>();

            boolean enforceFac = filters.isEnforceFacultyLimits();
            Map<Long, Integer> facultyWeekMinutes = new HashMap<>();
            Map<String, Integer> facultyDayMinutes = new HashMap<>();

            int remainingFreeWeek = Math.max(0, filters.getFreeSlotsPerWeek());
            Map<String, Integer> remainingFreePerDay = new HashMap<>();
            for (String d : workingDaysList) remainingFreePerDay.put(d, Math.max(0, filters.getFreeSlotsPerDay()));

            Random random = new Random();

            for (String dayStr : workingDaysList) {
                FacultyAvailability.DayOfWeekEnum day;
                try {
                    day = FacultyAvailability.DayOfWeekEnum.valueOf(dayStr);
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid day: " + dayStr);
                    continue;
                }

                LocalTime slotStart = startTime;
                String dayKey = dayStr;

                while (slotStart.plusMinutes(slotDuration).compareTo(endTime) <= 0) {
                    LocalTime slotEnd = slotStart.plusMinutes(slotDuration);

                    final LocalTime currentSlotStart = slotStart;
                    final LocalTime currentSlotEnd = slotEnd;

                    boolean isBreak = breaks.stream().anyMatch(b ->
                            currentSlotStart.isBefore(b[1]) && currentSlotEnd.isAfter(b[0])
                    );
                    if (isBreak) {
                        LocalTime breakEndTime = breaks.stream()
                                .filter(b -> currentSlotStart.isBefore(b[1]) && currentSlotEnd.isAfter(b[0]))
                                .map(b -> b[1])
                                .findFirst()
                                .orElse(currentSlotEnd);
                        slotStart = breakEndTime;
                        continue;
                    }

                    if (remainingFreePerDay.get(dayKey) > 0 || remainingFreeWeek > 0) {
                        if (remainingFreePerDay.get(dayKey) > 0) {
                            remainingFreePerDay.put(dayKey, remainingFreePerDay.get(dayKey) - 1);
                            slotStart = slotEnd;
                            continue;
                        } else if (remainingFreeWeek > 0) {
                            remainingFreeWeek--;
                            slotStart = slotEnd;
                            continue;
                        }
                    }

                    // --- Candidate subject list for the day ---
                    List<Subject> dayCandidates = new ArrayList<>(orderedSubjects);

                    // Labs on preferred days first
                    if (filters.getPreferredDaysForLabs() != null && !filters.getPreferredDaysForLabs().isEmpty()) {
                        List<Subject> prefLabs = new ArrayList<>();
                        List<Subject> others = new ArrayList<>();
                        for (Subject s : dayCandidates) {
                            List<String> pref = getPreferredDaysFor(filters.getPreferredDaysForLabs(), s.getSubjectName());
                            if (isLab(s.getSubjectName()) && pref != null) {
                                Set<String> prefUpper = pref.stream().map(d -> d.trim().toUpperCase()).collect(Collectors.toSet());
                                if (prefUpper.contains(dayKey)) {
                                    prefLabs.add(s);
                                    continue;
                                }
                            }
                            others.add(s);
                        }
                        dayCandidates.clear();
                        dayCandidates.addAll(prefLabs);
                        dayCandidates.addAll(others);
                    }

                    // Fairness: under-used subjects first
                    dayCandidates.sort(Comparator.comparingInt(s -> perWeekCounts.getOrDefault(s.getSubjectName(), 0)));

                    Subject chosen = null;

                    for (Subject candidate : dayCandidates) {
                        String subjName = candidate.getSubjectName();

                        List<FacultySubjectMap> eligibleFaculty = facultySubjectMapRepo
                                .findBySubject_SubjectIdAndSemesterAndIsActiveTrue(candidate.getSubjectId(), semesterNumber);

                        boolean foundFaculty = false;
                        for (FacultySubjectMap fsm : eligibleFaculty) {
                            Long facultyId = fsm.getFaculty().getFacultyId();
                            Long classId = Long.valueOf(collegeClass.getClassId());

                            boolean facultyFree = availabilityService.isFacultyFree(facultyId, day, slotStart, slotEnd);
                            boolean classFree = availabilityService.isClassFree(Math.toIntExact(classId), day, slotStart, slotEnd);
                            if (!facultyFree || !classFree) continue;

                            availabilityService.reserveSlot(
                                    facultyId, day, slotStart, slotEnd,
                                    Math.toIntExact(classId),
                                    candidate.getSubjectId(),
                                    semester, section,
                                    templateId, collegeId, adminId
                            );

                            perWeekCounts.put(subjName, perWeekCounts.getOrDefault(subjName, 0) + 1);
                            perDayCounts.computeIfAbsent(dayKey, k -> new HashMap<>())
                                    .put(subjName, safeNestedGet(perDayCounts, dayKey, subjName) + 1);
                            lastSubjectPerDay.put(dayKey, subjName);
                            chosen = candidate;
                            foundFaculty = true;
                            break;
                        }
                        if (foundFaculty) break;
                    }

                    slotStart = slotEnd;
                }
            }

            // Validation + API response
            Set<String> subjectsUsed = perWeekCounts.keySet();
            Set<String> subjectsExpected = subjects.stream().map(Subject::getSubjectName).collect(Collectors.toSet());
            Set<String> missing = new HashSet<>(subjectsExpected);
            missing.removeAll(subjectsUsed);
            Set<String> extras = new HashSet<>(subjectsUsed);
            extras.removeAll(subjectsExpected);

            Map<String, Object> report = new LinkedHashMap<>();
            report.put("status", "success");
            report.put("template", template.getTemplateName());
            report.put("semester", semester);
            report.put("section", section);
            report.put("expectedSubjects", subjectsExpected);
            report.put("scheduledSubjects", subjectsUsed);
            report.put("missingSubjects", missing);
            report.put("extraSubjects", extras);
            report.put("frequencyPerSubject", perWeekCounts);
            report.put("message", "✅ Timetable generated successfully with subject verification.");

            message = "✅ Timetable generated successfully for " + semester + " - " + section +
                    " using Template: " + template.getTemplateName();
            logAudit(adminId, collegeId, templateId, template.getTemplateName(), semester, section, "success", message);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);

        } catch (Exception e) {
            message = "❌ Error generating timetable: " + e.getMessage();
            logAudit(adminId, collegeId, templateId, null, toRoman(semesterNumber), section, "error", message);
            return "{\"status\":\"error\",\"message\":\"" + message + "\"}";
        }
    }

    // ✅ Audit logger method
    private void logAudit(Long adminId, Long collegeId, Integer templateId,
                          String templateName, String semester, String section,
                          String status, String message) {
        try {
            TimeTableAuditLog log = new TimeTableAuditLog(adminId, collegeId, templateId,
                    templateName, semester, section, status, message);
            auditLogRepo.save(log);
            System.out.println("🧾 [AUDIT] " + message + " (Admin: " + adminId + ")");
        } catch (Exception e) {
            System.out.println("⚠️ Failed to log audit entry: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helper utilities (unchanged)
    // -----------------------------------------------------------------------
    private static boolean isLab(String subjectName) {
        if (subjectName == null) return false;
        String s = subjectName.toLowerCase();
        return s.contains("lab") || s.contains("practical");
    }

    private static String canon(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    private static Map<String, String> buildCanonicalMap(List<Subject> subjects) {
        Map<String, String> m = new HashMap<>();
        for (Subject s : subjects) m.put(canon(s.getSubjectName()), s.getSubjectName());
        return m;
    }

    private static Integer getByCanonical(Map<String, Integer> map, String subjectName, Map<String, String> canonMap) {
        if (map == null || subjectName == null) return null;
        String subjCanon = canon(subjectName);
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            String keyCanon = canon(e.getKey());
            if (keyCanon.equals(subjCanon)) return e.getValue();
        }
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            String keyCanon = canon(e.getKey());
            if (subjCanon.contains(keyCanon) || keyCanon.contains(subjCanon)) return e.getValue();
        }
        return null;
    }

    private static List<String> getPreferredDaysFor(Map<String, List<String>> pref, String subjectName) {
        if (pref == null) return null;
        String subjCanon = canon(subjectName);
        for (Map.Entry<String, List<String>> e : pref.entrySet()) {
            String keyCanon = canon(e.getKey());
            if (keyCanon.equals(subjCanon) || subjCanon.contains(keyCanon) || keyCanon.contains(subjCanon))
                return e.getValue();
        }
        return null;
    }

    private static int safeNestedGet(Map<String, Map<String, Integer>> outer, String day, String subject) {
        if (outer == null || day == null || subject == null) return 0;
        Map<String, Integer> inner = outer.get(day);
        if (inner == null) return 0;
        for (Map.Entry<String, Integer> e : inner.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(subject)) return e.getValue();
        }
        return 0;
    }

    private static String facultyDayKey(Long facultyId, String day) {
        return facultyId + "|" + day;
    }

    public FacultyAvailabilityService getAvailabilityService() {
        return this.availabilityService;
    }




    public String deleteTimetableForClass(String adminEmail,
                                          String adminPassword,
                                          String semesterNumber,
                                          String section) throws Exception {

        // 🔐 Step 1: Authenticate admin
        Admin admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalAccessException("Invalid admin email or password"));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword()))
            throw new IllegalAccessException("Invalid admin credentials");

        if (!admin.isVerified())
            throw new IllegalAccessException("Admin account not verified.");

        // 🏫 Step 2: Fetch admin's college
        College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("College not found for admin"));

        Long collegeId = college.getCollegeId();

        // Convert semester → Roman
        String semester = toRoman(semesterNumber);

        // 🧩 Step 3: Fetch class row
        CollegeClass collegeClass = classRepo
                .findByCollege_CollegeIdAndSemesterAndSection(collegeId, semester, section)
                .orElseThrow(() -> new IllegalStateException("Class not found for semester & section"));

        Integer classId = collegeClass.getClassId();

        // ❌ Step 4: Delete timetable
        int freed = availabilityRepo.deleteByClassAndSemesterAndSection(classId, semester, section);

        String message = "Deleted " + freed + " timetable slots for " + semester + "-" + section;

        // 🧾 Step 5: Log audit entry
        logAudit(
                admin.getAdminId(),
                collegeId,
                null,
                "DELETE_TIMETABLE",
                semester,
                section,
                "success",
                message
        );

        // ✉️ Step 6: Send email notification
        Map<String, String> values = Map.of(
                "ADMIN_NAME", admin.getName(),
                "SEMESTER", semester,
                "SECTION", section,
                "COLLEGE", college.getCollegeName()
        );

        emailService.sendEmailFromTemplate(
                "timetable_deleted_notification.html",
                admin.getEmail(),
                values
        );

        return "✅ " + message;
    }

}
