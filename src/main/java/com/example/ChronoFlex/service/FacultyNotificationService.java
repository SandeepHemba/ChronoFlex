package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.EnableNotificationRequest;
import com.example.ChronoFlex.dto.GlobalToggleRequest;
import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class FacultyNotificationService {

    private final FacultyNotificationSettingRepository settingRepo;
    private final com.example.chronoflex.repository.FacultyAvailabilityRepository availabilityRepo;
    private final FacultyRepository facultyRepo;
    private final AdminRepository adminRepo;
    private final SubjectRepository subjectRepo;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final CollegeRepository collegeRepo;


    public FacultyNotificationService(
            FacultyNotificationSettingRepository settingRepo,
            com.example.chronoflex.repository.FacultyAvailabilityRepository availabilityRepo,
            FacultyRepository facultyRepo,
            AdminRepository adminRepo,
            SubjectRepository subjectRepo,
            CollegeRepository collegeRepo,   // <-- ADD THIS
            EmailService emailService
    ) {
        this.settingRepo = settingRepo;
        this.availabilityRepo = availabilityRepo;
        this.facultyRepo = facultyRepo;
        this.adminRepo = adminRepo;
        this.subjectRepo = subjectRepo;
        this.collegeRepo = collegeRepo;     // <-- ADD THIS
        this.emailService = emailService;
    }

    // ============================================================
    // ENABLE / DISABLE REMINDERS
    // ============================================================
    public String updateNotificationSetting(EnableNotificationRequest req) throws Exception {

        Admin admin = adminRepo.findByEmail(req.getAdminEmail())
                .orElseThrow(() -> new IllegalAccessException("Invalid admin email"));

        if (!encoder.matches(req.getAdminPassword(), admin.getPassword()))
            throw new IllegalAccessException("Invalid admin password");

        College adminCollege = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("Admin college not found"));

        Faculty faculty = null;

        // 1️⃣ Search by EMAIL
        if (req.getFacultyEmail() != null && !req.getFacultyEmail().isBlank()) {
            faculty = facultyRepo.findByEmail(req.getFacultyEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Faculty not found with email"));
        }

        // 2️⃣ If not email → search by Name
        else if (req.getFacultyName() != null && !req.getFacultyName().isBlank()) {
            faculty = (Faculty) facultyRepo.findByNameAndCollege_CollegeId(req.getFacultyName(), adminCollege.getCollegeId())
                    .orElseThrow(() -> new IllegalArgumentException("Faculty not found with given name"));
        }

        else {
            throw new IllegalArgumentException("Provide either facultyEmail or facultyName");
        }

        if (!Objects.equals(faculty.getCollege().getCollegeId(), adminCollege.getCollegeId()))
            throw new IllegalAccessException("Faculty does not belong to your college");

        FacultyNotificationSetting setting =
                settingRepo.findByFacultyId(faculty.getFacultyId())
                        .orElse(new FacultyNotificationSetting(faculty.getFacultyId(), adminCollege.getCollegeId(), false));

        setting.setEnabled(req.isEnabled());
        setting.setCollegeId(adminCollege.getCollegeId());
        settingRepo.save(setting);

        return setting.isEnabled()
                ? "Notifications ENABLED for: " + faculty.getName()
                : "Notifications DISABLED for: " + faculty.getName();
    }


    // ============================================================
    // ENABLE / DISABLE REMINDERS --> Global(Admins College)
    // ============================================================
    public String updateAllFacultyNotifications(GlobalToggleRequest req) throws Exception {

        Admin admin = adminRepo.findByEmail(req.getAdminEmail())
                .orElseThrow(() -> new IllegalAccessException("Invalid admin email"));

        if (!encoder.matches(req.getAdminPassword(), admin.getPassword()))
            throw new IllegalAccessException("Invalid admin password");

        College adminCollege = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("Admin college not found"));

        List<Faculty> facultyList =
                facultyRepo.findByCollege_CollegeId(adminCollege.getCollegeId());

        int count = 0;

        for (Faculty f : facultyList) {
            FacultyNotificationSetting setting =
                    settingRepo.findByFacultyId(f.getFacultyId())
                            .orElse(new FacultyNotificationSetting(f.getFacultyId(), adminCollege.getCollegeId(), false));

            setting.setEnabled(req.isEnabled());
            settingRepo.save(setting);
            count++;
        }

        return req.isEnabled()
                ? "Notifications ENABLED for all " + count + " faculty"
                : "Notifications DISABLED for all " + count + " faculty";
    }





    // ============================================================
    // CRON JOB — Runs every minute, sends email 10 minutes before class
    // ============================================================
    @Scheduled(cron = "0 * * * * *") // every minute
    public void sendClassReminders() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusMinutes(10);

        LocalTime from = target.truncatedTo(ChronoUnit.MINUTES).toLocalTime();
        LocalTime to = from.plusMinutes(1);

        String dow = target.getDayOfWeek().name().substring(0,3); // MON, TUE...
        FacultyAvailability.DayOfWeekEnum day;

        try {
            day = FacultyAvailability.DayOfWeekEnum.valueOf(dow);
        } catch (Exception e) {
            return;
        }

        List<FacultyAvailability> upcoming =
                availabilityRepo.findByDayOfWeekAndStartTimeBetween(day, from, to);

        if (upcoming.isEmpty()) return;

        Map<Long, List<FacultyAvailability>> byFaculty =
                upcoming.stream().collect(Collectors.groupingBy(FacultyAvailability::getFacultyId));

        for (Long facultyId : byFaculty.keySet()) {

            Faculty faculty = facultyRepo.findById(facultyId).orElse(null);
            if (faculty == null || !faculty.getIsActive()) continue;

            Optional<FacultyNotificationSetting> opt = settingRepo.findByFacultyId(facultyId);
            if (opt.isEmpty() || !opt.get().isEnabled()) continue;

            String email = faculty.getEmail();
            if (email == null) continue;

            StringBuilder rows = new StringBuilder();

            for (FacultyAvailability fa : byFaculty.get(facultyId)) {

                AtomicReference<String> subjectName = new AtomicReference<>("Unknown");
                if (fa.getSubjectId() != null)
                    subjectRepo.findById(fa.getSubjectId()).ifPresent(s -> subjectName.set(s.getSubjectName()));

                rows.append("<tr>")
                        .append("<td>").append(fa.getDayOfWeek()).append("</td>")
                        .append("<td>").append(fa.getStartTime()).append(" - ").append(fa.getEndTime()).append("</td>")
                        .append("<td>").append(subjectName.get()).append("</td>")
                        .append("<td>").append(fa.getSemester()).append(" ").append(fa.getSection()).append("</td>")
                        .append("</tr>");
            }

            Map<String, String> values = new HashMap<>();
            values.put("FACULTY_NAME", faculty.getName());
            values.put("TIMETABLE_ROWS", rows.toString());
            values.put("REMINDER_MINUTES", "10");

            try {
                emailService.sendEmailFromTemplate("faculty_reminder_10min.html", email, values);
                emailService.logEmailActivity(email, "Class Reminder Sent", "10-min reminder sent");
            } catch (Exception ignored) {}
        }
    }

    public Map<String, Object> getStatus(Long facultyId) {
        Map<String, Object> resp = new HashMap<>();

        FacultyNotificationSetting setting =
                settingRepo.findByFacultyId(facultyId).orElse(null);

        resp.put("facultyId", facultyId);
        resp.put("enabled", setting != null && setting.isEnabled());

        return resp;
    }

}
