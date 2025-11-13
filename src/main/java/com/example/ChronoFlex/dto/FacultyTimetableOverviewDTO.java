package com.example.ChronoFlex.dto;

import java.util.List;

public class FacultyTimetableOverviewDTO {
    private Long facultyId;
    private String facultyName;
    private List<TimetableSlotDTO> timetable;

    public FacultyTimetableOverviewDTO(Long facultyId, String facultyName, List<TimetableSlotDTO> timetable) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.timetable = timetable;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public List<TimetableSlotDTO> getTimetable() {
        return timetable;
    }
}
