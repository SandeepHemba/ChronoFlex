package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByCollegeCodeAndRegId(String collegeCode, String regId);

    Optional<Student> findByEmail(String email);
}