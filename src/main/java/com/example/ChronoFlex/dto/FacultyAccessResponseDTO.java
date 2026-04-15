package com.example.ChronoFlex.dto;

import java.util.List;

public class FacultyAccessResponseDTO {

    private Long facultyId;
    private String facultyName;
    private String department;

    private List<SubjectInfo> subjects;
    private List<ClassInfo> classes;

    // Constructor
    public FacultyAccessResponseDTO(Long facultyId,
                                    String facultyName,
                                    String department,
                                    List<SubjectInfo> subjects,
                                    List<ClassInfo> classes) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.department = department;
        this.subjects = subjects;
        this.classes = classes;
    }

    // Getters
    public Long getFacultyId() { return facultyId; }
    public String getFacultyName() { return facultyName; }
    public String getDepartment() { return department; }
    public List<SubjectInfo> getSubjects() { return subjects; }
    public List<ClassInfo> getClasses() { return classes; }

    // ==============================
    // Inner Static Classes
    // ==============================

    public static class SubjectInfo {
        private Long subjectId;
        private String subjectName;
        private String semester;

        public SubjectInfo(Long subjectId, String subjectName, String semester) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.semester = semester;
        }

        public Long getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public String getSemester() { return semester; }
    }

    public static class ClassInfo {
        private Integer classId;
        private String className;
        private String semester;
        private String section;

        public ClassInfo(Integer classId, String className, String semester, String section) {
            this.classId = classId;
            this.className = className;
            this.semester = semester;
            this.section = section;
        }

        public Integer getClassId() { return classId; }
        public String getClassName() { return className; }
        public String getSemester() { return semester; }
        public String getSection() { return section; }
    }
}