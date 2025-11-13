package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacultySubjectMapRepository extends JpaRepository<FacultySubjectMap, Long> {

    // Fetch by college
    List<FacultySubjectMap> findByCollege(College college);

    // Fetch by faculty
    List<FacultySubjectMap> findByFaculty(Faculty faculty);

    // Fetch by subject
    List<FacultySubjectMap> findBySubject(Subject subject);

    // Fetch by college + faculty
    List<FacultySubjectMap> findByCollegeAndFaculty(College college, Faculty faculty);

    // Fetch active mappings
    List<FacultySubjectMap> findByCollegeAndIsActiveTrue(College college);

    // Check duplicate
    boolean existsByFacultyAndSubject(Faculty faculty, Subject subject);

    List<FacultySubjectMap> findByCollegeAndDepartmentContainingIgnoreCaseAndSemesterContainingIgnoreCase(College college, String s, String s1);

    List<FacultySubjectMap> findByCollege_CollegeId(Long collegeId);

    List<FacultySubjectMap> findBySubject_SubjectIdAndSemesterAndIsActiveTrue(Long subjectId, String semester);
}
