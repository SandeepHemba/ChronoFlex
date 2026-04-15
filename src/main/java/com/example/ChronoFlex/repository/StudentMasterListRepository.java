package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.StudentMasterList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentMasterListRepository
        extends JpaRepository<StudentMasterList, Long> {

    Optional<StudentMasterList>
    findByCollegeCodeAndRegId(String collegeCode, String regId);

    List<StudentMasterList> findByCollegeCode(String collegeCode);

    List<StudentMasterList>
    findByCollegeCodeAndSemesterAndSection(
            String collegeCode,
            String semester,
            String section);
}