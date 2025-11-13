package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.FacultyAvailability;
import com.example.ChronoFlex.model.FacultyAvailability.DayOfWeekEnum;
import com.example.chronoflex.repository.FacultyAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class FacultyAvailabilityService {

    private final FacultyAvailabilityRepository repo;

    public FacultyAvailabilityService(FacultyAvailabilityRepository repo) {
        this.repo = repo;
    }

    /**
     * Checks if a faculty is free during the given slot
     */
    public boolean isFacultyFree(Long facultyId, DayOfWeekEnum dayOfWeek, LocalTime start, LocalTime end) {
        List<FacultyAvailability> conflicts = repo.findConflicts(facultyId, dayOfWeek.name(), start, end);
        return conflicts.isEmpty();
    }

    /**
     * Checks if a class is free during the given slot
     */
    public boolean isClassFree(Integer classId, DayOfWeekEnum dayOfWeek, LocalTime start, LocalTime end) {
        List<FacultyAvailability> conflicts = repo.findConflictsByClass(classId, dayOfWeek.name(), start, end);
        return conflicts.isEmpty();
    }

    /**
     * Checks for conflicts and if none, persists a BUSY slot.
     * Throws IllegalStateException if conflict found.
     */
    @Transactional
    public FacultyAvailability reserveSlot(
            Long facultyId,
            DayOfWeekEnum dayOfWeek,
            LocalTime start,
            LocalTime end,
            Integer classId,
            Long subjectId,
            String semester,
            String section,
            Integer templateId,
            Long collegeId,
            Long adminId) {

        // 1) check faculty conflict
        if (!isFacultyFree(facultyId, dayOfWeek, start, end)) {
            throw new IllegalStateException("Faculty is already BUSY during the requested time slot.");
        }

        // 2) check class conflict
        if (!isClassFree(classId, dayOfWeek, start, end)) {
            throw new IllegalStateException("Class is already BUSY during the requested time slot.");
        }

        // 3) save BUSY slot
        FacultyAvailability slot = new FacultyAvailability();
        slot.setFacultyId(facultyId);
        slot.setDayOfWeek(dayOfWeek);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setClassId(classId);
        slot.setSubjectId(subjectId);
        slot.setSemester(semester);
        slot.setSection(section);
        slot.setTemplateId(templateId);
        slot.setStatus(FacultyAvailability.Status.BUSY);
        slot.setCollegeId(collegeId);
        slot.setCreatedBy(adminId);

        return repo.save(slot);
    }

    /**
     * Frees all slots for a given template (e.g., when regenerating a timetable)
     */
    @Transactional
    public void freeSlotsByTemplate(Integer templateId) {
        List<FacultyAvailability> slots = repo.findByTemplateId(templateId);
        for (FacultyAvailability s : slots) {
            s.setStatus(FacultyAvailability.Status.FREE);
            s.setClassId(null);
            s.setSubjectId(null);
            s.setSemester(null);
            s.setSection(null);
            s.setTemplateId(null);
        }
        repo.saveAll(slots);
    }
}
