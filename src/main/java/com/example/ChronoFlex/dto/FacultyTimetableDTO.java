package com.example.ChronoFlex.dto;

import java.time.LocalTime;

public class FacultyTimetableDTO {
    private String facultyName;
    private String subjectName;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String section;
    private String semester;

    public FacultyTimetableDTO(String facultyName, String subjectName, String dayOfWeek,
                               LocalTime startTime, LocalTime endTime, String section, String semester) {
        this.facultyName = facultyName;
        this.subjectName = subjectName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.section = section;
        this.semester = semester;
    }

    // getters and setters

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
