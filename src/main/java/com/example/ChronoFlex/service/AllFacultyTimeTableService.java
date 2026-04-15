package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.FacultyTimetableOverviewDTO;
import com.example.ChronoFlex.dto.TimetableSlotDTO;
import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AllFacultyTimeTableService {

    private final FacultyRepository facultyRepo;
    private final com.example.ChronoFlex.repository.FacultyAvailabilityRepository facultyAvailabilityRepo;
    private final SubjectRepository subjectRepo;
    private final AdminRepository adminRepo;
    private final CollegeRepository collegeRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AllFacultyTimeTableService(
            FacultyRepository facultyRepo,
            com.example.ChronoFlex.repository.FacultyAvailabilityRepository facultyAvailabilityRepo,
            SubjectRepository subjectRepo,
            AdminRepository adminRepo,
            CollegeRepository collegeRepo
    ) {
        this.facultyRepo = facultyRepo;
        this.facultyAvailabilityRepo = facultyAvailabilityRepo;
        this.subjectRepo = subjectRepo;
        this.adminRepo = adminRepo;
        this.collegeRepo = collegeRepo;
    }

    /**
     * ✅ Returns all faculty timetables for the college linked to the authenticated admin.
     */
    public List<FacultyTimetableOverviewDTO> getAllFacultyTimetables(String adminEmail, String adminPassword)
            throws IllegalAccessException {

        // 🧩 Step 1: Authenticate admin using BCrypt
        Admin admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalAccessException("Invalid admin email or password."));

        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            throw new IllegalAccessException("Invalid admin credentials.");
        }

        if (!admin.isVerified()) {
            throw new IllegalAccessException("Admin account not verified.");
        }

        // 🧩 Step 2: Get the admin’s college by code
        College college = collegeRepo.findByCollegeCode(admin.getCollegeCode())
                .orElseThrow(() -> new IllegalStateException("College not found for admin’s college code."));

        Long collegeId = college.getCollegeId();

        // 🧩 Step 3: Fetch all faculties for this college
        List<Faculty> faculties = facultyRepo.findByCollege_CollegeId(collegeId);
        if (faculties.isEmpty()) {
            throw new IllegalStateException("No faculties found for the admin’s college.");
        }

        List<FacultyTimetableOverviewDTO> overviewList = new ArrayList<>();

        // 🧩 Step 4: Build each faculty’s timetable
        for (Faculty faculty : faculties) {
            Long facultyId = faculty.getFacultyId();
            String facultyName = faculty.getName();

            List<FacultyAvailability> slots = facultyAvailabilityRepo.findByFacultyId(facultyId);

            List<TimetableSlotDTO> timetable = slots.stream().map(slot -> {
                        String subjectName = subjectRepo.findById(slot.getSubjectId())
                                .map(Subject::getSubjectName)
                                .orElse("Unknown Subject");

                        return new TimetableSlotDTO(
                                slot.getDayOfWeek().name(),
                                slot.getStartTime().toString(),
                                slot.getEndTime().toString(),
                                slot.getSection(),
                                slot.getSemester(),
                                subjectName,
                                facultyName
                        );
                    })
                    .sorted(Comparator
                            .comparing(TimetableSlotDTO::getDay)
                            .thenComparing(TimetableSlotDTO::getStartTime))
                    .collect(Collectors.toList());

            overviewList.add(new FacultyTimetableOverviewDTO(facultyId, facultyName, timetable));
        }

        // 🧩 Step 5: Sort alphabetically
        overviewList.sort(Comparator.comparing(FacultyTimetableOverviewDTO::getFacultyName));

        return overviewList;
    }


    /**
     * ✅ Fetch all faculty timetables based on the admin’s linked college.
     * Used by FacultyTimetableEmailService.
     */
    public List<FacultyTimetableOverviewDTO> getAllFacultyTimetablesByAdmin(Admin admin)
            throws IllegalAccessException {

        if (admin == null) {
            throw new IllegalArgumentException("Admin cannot be null.");
        }

        String collegeCode = admin.getCollegeCode();
        if (collegeCode == null || collegeCode.isEmpty()) {
            throw new IllegalStateException("Admin is not linked to any college.");
        }

        // Step 1: Find any faculty linked to that admin’s college code
        List<Faculty> faculties = facultyRepo.findAll();
        Faculty anyFaculty = faculties.stream()
                .filter(f -> f.getCollege() != null &&
                        collegeCode.equalsIgnoreCase(f.getCollege().getCollegeCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No faculties found for the admin’s linked college: " + collegeCode));

        Long collegeId = anyFaculty.getCollege().getCollegeId();

        // Step 2: Call the correct 2-parameter version of your method
        // ⚠️ It seems your current method signature has only two args, so we pass them.
        return getAllFacultyTimetables(
                admin.getEmail(),
                admin.getPassword()
        );
    }


    /**
     * ✅ Fetch timetable for a specific faculty using email + password
     */
    public FacultyTimetableOverviewDTO getFacultyTimetable(String email, String password)
            throws IllegalAccessException {

        // 🔐 Step 1: Authenticate faculty
        Faculty faculty = facultyRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalAccessException("Invalid faculty email or password."));

        if (!passwordEncoder.matches(password, faculty.getPassword())) {
            throw new IllegalAccessException("Invalid faculty credentials.");
        }

        Long facultyId = faculty.getFacultyId();
        String facultyName = faculty.getName();

        // 📅 Step 2: Fetch timetable slots
        List<FacultyAvailability> slots = facultyAvailabilityRepo.findByFacultyId(facultyId);

        List<TimetableSlotDTO> timetable = slots.stream().map(slot -> {
                    String subjectName = subjectRepo.findById(slot.getSubjectId())
                            .map(Subject::getSubjectName)
                            .orElse("Unknown Subject");

                    return new TimetableSlotDTO(
                            slot.getDayOfWeek().name(),
                            slot.getStartTime().toString(),
                            slot.getEndTime().toString(),
                            slot.getSection(),
                            slot.getSemester(),
                            subjectName,
                            facultyName
                    );
                })
                .sorted(Comparator
                        .comparing(TimetableSlotDTO::getDay)
                        .thenComparing(TimetableSlotDTO::getStartTime))
                .toList();

        return new FacultyTimetableOverviewDTO(facultyId, facultyName, timetable);
    }

}
