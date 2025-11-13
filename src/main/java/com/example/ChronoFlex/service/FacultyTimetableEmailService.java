package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.FacultyTimetableOverviewDTO;
import com.example.ChronoFlex.dto.TimetableSlotDTO;
import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Faculty;
import com.example.ChronoFlex.repository.AdminRepository;
import com.example.ChronoFlex.repository.CollegeRepository;
import com.example.ChronoFlex.repository.FacultyRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacultyTimetableEmailService {

    private final AdminRepository adminRepo;
    private final FacultyRepository facultyRepo;
    private final CollegeRepository collegeRepo;
    private final AllFacultyTimeTableService allFacultyTimeTableService;
    private final JavaMailSender mailSender;
    private final EmailService emailLogService; // ✅ integrated email logger
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public FacultyTimetableEmailService(AdminRepository adminRepo,
                                        FacultyRepository facultyRepo,
                                        CollegeRepository collegeRepo,
                                        AllFacultyTimeTableService allFacultyTimeTableService,
                                        JavaMailSender mailSender,
                                        EmailService emailLogService) {
        this.adminRepo = adminRepo;
        this.facultyRepo = facultyRepo;
        this.collegeRepo = collegeRepo;
        this.allFacultyTimeTableService = allFacultyTimeTableService;
        this.mailSender = mailSender;
        this.emailLogService = emailLogService;
    }

    /**
     * ✅ Sends timetable emails to all faculties of the admin’s college and logs everything.
     */
    public int sendAllFacultyTimetables(String adminEmail, String adminPassword) throws Exception {
        // Step 1: Authenticate admin using BCrypt
        Admin admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalAccessException("Invalid admin email or password."));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new IllegalAccessException("Invalid admin credentials.");
        }

        if (!admin.isVerified()) {
            throw new IllegalAccessException("Admin account not verified.");
        }

        // Step 2: Get admin’s linked college
        College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("College not found for admin’s code: " + admin.getCollegeCode()));

        // Step 3: Fetch faculty timetables
        List<FacultyTimetableOverviewDTO> allTimetables =
                allFacultyTimeTableService.getAllFacultyTimetables(admin.getEmail(), adminPassword);

        if (allTimetables.isEmpty()) {
            throw new IllegalStateException("No faculty timetables found for this college.");
        }

        // Step 4: Load HTML email template
        String htmlTemplate = loadTemplate("templates/email/faculty_timetable_template.html");
        int sentCount = 0;
        int failedCount = 0;

        // Step 5: Send and log each email
        for (FacultyTimetableOverviewDTO facultyDTO : allTimetables) {
            Faculty faculty = facultyRepo.findById(facultyDTO.getFacultyId()).orElse(null);
            if (faculty == null || faculty.getEmail() == null || facultyDTO.getTimetable().isEmpty()) continue;

            // ✅ Get college name dynamically
            String collegeName = (faculty.getCollege() != null)
                    ? faculty.getCollege().getCollegeName()
                    : "Your College";

            String tableHTML = buildTableHTML(facultyDTO.getTimetable());
            String personalizedHtml = htmlTemplate
                    .replace("{{FACULTY_NAME}}", facultyDTO.getFacultyName())
                    .replace("{{COLLEGE_NAME}}", collegeName)
                    .replace("{{TIMETABLE_ROWS}}", tableHTML);


            try {
                sendEmail(faculty.getEmail(),
                        "Your Faculty Timetable - ChronoFlex",
                        personalizedHtml);

                // ✅ Log successful email
                emailLogService.logEmailActivity(
                        faculty.getEmail(),
                        "Faculty Timetable Sent",
                        "Timetable successfully emailed to " + faculty.getName() + " (" + faculty.getEmail() + ")"
                );
                sentCount++;
            } catch (Exception e) {
                failedCount++;
                // ✅ Log failed email
                emailLogService.logEmailActivity(
                        faculty.getEmail(),
                        "Faculty Timetable Email Failed",
                        "Failed to send timetable to " + faculty.getName() + " (" + faculty.getEmail() + ") due to: " + e.getMessage()
                );
            }
        }

        // Step 6: Add one summary audit log
        String summaryMessage = String.format(
                "Admin '%s' sent timetables to %d faculties (%d success, %d failed) at %s",
                admin.getName(),
                (sentCount + failedCount),
                sentCount,
                failedCount,
                LocalDateTime.now().toString()
        );

        emailLogService.logEmailActivity(
                admin.getEmail(),
                "Faculty Timetable Email Batch Summary",
                summaryMessage
        );

        return sentCount;
    }

    // 🧩 Load HTML email template from resources
    private String loadTemplate(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
    }

    // 🧩 Send email with HTML body
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // 🧩 Build Timetable in Day x Time Grid format
    private String buildTableHTML(List<TimetableSlotDTO> slots) {
        // 1️⃣ Define day order
        List<String> dayOrder = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT");

        // 2️⃣ Define unique time slots (sorted)
        List<String> timeSlots = slots.stream()
                .map(s -> s.getStartTime() + " - " + s.getEndTime())
                .distinct()
                .sorted()
                .toList();

        // 3️⃣ Map<Day, Map<TimeSlot, Subject>>
        Map<String, Map<String, String>> timetableMap = new LinkedHashMap<>();
        for (String day : dayOrder) {
            timetableMap.put(day, new LinkedHashMap<>());
            for (String time : timeSlots) {
                timetableMap.get(day).put(time, "—");
            }
        }

        // 4️⃣ Fill timetable map with subjects
        for (TimetableSlotDTO slot : slots) {
            String day = slot.getDay();
            String time = slot.getStartTime() + " - " + slot.getEndTime();
            String subject = slot.getSubjectName() + " (" + slot.getSemester() + " " + slot.getSection() + ")";
            if (timetableMap.containsKey(day) && timetableMap.get(day).containsKey(time)) {
                timetableMap.get(day).put(time, subject);
            }
        }

        // 5️⃣ Build HTML table
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1' cellspacing='0' cellpadding='6' style='border-collapse:collapse;width:100%;font-family:Arial;'>");
        sb.append("<thead><tr><th>Day</th>");
        for (String time : timeSlots) {
            sb.append("<th>").append(time).append("</th>");
        }
        sb.append("</tr></thead><tbody>");

        for (String day : dayOrder) {
            sb.append("<tr><td><b>").append(day).append("</b></td>");
            for (String time : timeSlots) {
                sb.append("<td>").append(timetableMap.get(day).get(time)).append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");
        return sb.toString();
    }

}
