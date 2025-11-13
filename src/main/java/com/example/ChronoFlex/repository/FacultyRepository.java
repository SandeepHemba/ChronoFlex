package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmailAndCollege_CollegeId(String email, Long collegeId);

    Optional<Faculty> findByEmail(String email);

    @Query("SELECT f FROM Faculty f WHERE f.college.collegeId = :collegeId "
            + "AND (:department IS NULL OR f.department = :department) "
            + "AND (:qualification IS NULL OR f.qualification = :qualification) "
            + "AND (:active IS NULL OR f.isActive = :active)")
    List<Faculty> findFilteredFaculties(
            @Param("collegeId") Long collegeId,
            @Param("department") String department,
            @Param("qualification") String qualification,
            @Param("active") Boolean active
    );

    List<Faculty> findByNameAndCollege(String name, College college);

    Optional<Object> findByName(String facultyName);

    List<Faculty> findByCollege_CollegeId(Long collegeId);

}
