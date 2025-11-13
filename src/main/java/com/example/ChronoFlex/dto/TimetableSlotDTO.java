package com.example.ChronoFlex.dto;

public class TimetableSlotDTO {
    private String day;
    private String startTime;
    private String endTime;
    private String section;
    private String semester;
    private String subjectName;
    private String facultyName;

    public TimetableSlotDTO(String day, String startTime, String endTime,
                            String section, String semester, String subjectName, String facultyName) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.section = section;
        this.semester = semester;
        this.subjectName = subjectName;
        this.facultyName = facultyName;
    }

    // getters and setters
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
}
