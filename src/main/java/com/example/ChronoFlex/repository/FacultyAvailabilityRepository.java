package com.example.chronoflex.repository;

import com.example.ChronoFlex.model.FacultyAvailability;
import com.example.ChronoFlex.model.FacultyAvailability.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface FacultyAvailabilityRepository extends JpaRepository<FacultyAvailability, Long> {

    // Find all slots for a faculty on a given day
    List<FacultyAvailability> findByFacultyIdAndDayOfWeek(Long facultyId, DayOfWeekEnum dayOfWeek);

    // Native query to find overlapping BUSY slots for a faculty on a given day.
    @Query(value = "SELECT * FROM faculty_availability fa " +
            "WHERE fa.faculty_id = :facultyId " +
            "  AND fa.day_of_week = :dayOfWeek " +
            "  AND fa.status = 'BUSY' " +
            "  AND NOT ( :newEnd <= fa.start_time OR :newStart >= fa.end_time )",
            nativeQuery = true)
    List<FacultyAvailability> findConflicts(
            @Param("facultyId") Long facultyId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("newStart") LocalTime newStart,
            @Param("newEnd") LocalTime newEnd);

    // Optionally, find by template to clear later
    List<FacultyAvailability> findByTemplateId(Integer templateId);

    //List<FacultyAvailability> findByCollegeClass_ClassId(Integer classId);

    List<FacultyAvailability> findByClassId(Integer classId);

    // Native query to find overlapping BUSY slots for a class on a given day
    @Query(value = "SELECT * FROM faculty_availability fa " +
            "WHERE fa.class_id = :classId " +
            "  AND fa.day_of_week = :dayOfWeek " +
            "  AND fa.status = 'BUSY' " +
            "  AND NOT ( :newEnd <= fa.start_time OR :newStart >= fa.end_time )",
            nativeQuery = true)
    List<FacultyAvailability> findConflictsByClass(
            @Param("classId") Integer classId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("newStart") LocalTime newStart,
            @Param("newEnd") LocalTime newEnd);


    // ✅ Fetch all slots for a given faculty
    List<FacultyAvailability> findByFacultyId(Long facultyId);

    // ✅ Fetch all slots for a specific college
    List<FacultyAvailability> findByCollegeId(Long collegeId);

}
