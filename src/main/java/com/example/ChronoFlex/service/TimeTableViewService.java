package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.TimetableSlotDTO;
import com.example.ChronoFlex.model.*;
import com.example.ChronoFlex.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TimeTableViewService {

    private final CollegeClassRepository classRepo;
    private final com.example.chronoflex.repository.FacultyAvailabilityRepository facultyAvailabilityRepo;
    private final SubjectRepository subjectRepo;
    private final FacultyRepository facultyRepo;

    public TimeTableViewService(CollegeClassRepository classRepo,
                                com.example.chronoflex.repository.FacultyAvailabilityRepository facultyAvailabilityRepo,
                                SubjectRepository subjectRepo,
                                FacultyRepository facultyRepo) {
        this.classRepo = classRepo;
        this.facultyAvailabilityRepo = facultyAvailabilityRepo;
        this.subjectRepo = subjectRepo;
        this.facultyRepo = facultyRepo;
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

    public List<TimetableSlotDTO> getTimeTable(Long collegeId, String semesterNumber, String section) {

        String semester = toRoman(semesterNumber);

        Optional<CollegeClass> optClass = classRepo.findByCollege_CollegeIdAndSemesterAndSection(
                collegeId, semester, section
        );
        if (optClass.isEmpty()) {
            return Collections.emptyList();
        }

        CollegeClass collegeClass = optClass.get();

        List<FacultyAvailability> slots = facultyAvailabilityRepo.findByClassId(collegeClass.getClassId());

        List<TimetableSlotDTO> timetable = new ArrayList<>();

        for (FacultyAvailability slot : slots) {

            String subjectName = subjectRepo.findById(slot.getSubjectId())
                    .map(s -> s.getSubjectName())
                    .orElse("Unknown Subject");

            String facultyName = facultyRepo.findById(slot.getFacultyId())
                    .map(f -> f.getName())
                    .orElse("Unknown Faculty");

            timetable.add(new TimetableSlotDTO(
                    slot.getDayOfWeek().name(),
                    slot.getStartTime().toString(),
                    slot.getEndTime().toString(),
                    slot.getSection(),
                    slot.getSemester(),
                    subjectName,
                    facultyName
            ));
        }

        return timetable;
    }
}
