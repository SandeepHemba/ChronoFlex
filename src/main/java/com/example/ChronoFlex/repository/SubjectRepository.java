package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // Fetch subjects with optional type filter
    List<Subject> findByCollege_CollegeIdAndDepartmentContainingIgnoreCaseAndSemesterContainingIgnoreCaseAndSubjectType(
            Long collegeId, String department, String semester, Subject.SubjectType subjectType);

    List<Subject> findByCollege_CollegeIdAndDepartmentContainingIgnoreCaseAndSemesterContainingIgnoreCase(
            Long collegeId, String department, String semester);

    Optional<Object> findBySubjectCodeAndCollege(String subjectCode, College college);

    Optional<Object> findBySubjectCode(String subjectCode);

    List<Subject> findByCollege_CollegeId(Long collegeId);

    List<Subject> findByCollege_CollegeIdAndSemesterAndIsActiveTrue(Long collegeId, String semester);
}
