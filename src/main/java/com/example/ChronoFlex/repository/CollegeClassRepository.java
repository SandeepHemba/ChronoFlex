package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.CollegeClass;
import com.example.ChronoFlex.model.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeClassRepository extends JpaRepository<CollegeClass, Integer> {

    List<CollegeClass> findByCollege(College college);

    List<CollegeClass> findByCollegeAndDepartmentAndSemesterAndStatus(
            College college, String department, String semester, CollegeClass.Status status
    );

    List<CollegeClass> findByCollegeAndSemester(College college, String semester);

    List<CollegeClass> findByCollegeAndStatus(College college, CollegeClass.Status status);

    List<CollegeClass> findByCollegeAndSemesterAndStatus(College college, String semester, CollegeClass.Status status);

    List<CollegeClass> findByCollege_CollegeId(Long collegeId);

     // Return list instead of Optional to handle multiple classes
    Optional<CollegeClass> findByCollege_CollegeIdAndSemesterAndSection(Long collegeId, String semester, String section);
}
