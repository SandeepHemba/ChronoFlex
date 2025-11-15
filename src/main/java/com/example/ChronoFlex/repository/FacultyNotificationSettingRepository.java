package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.FacultyNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface FacultyNotificationSettingRepository extends JpaRepository<FacultyNotificationSetting, Long> {

    Optional<FacultyNotificationSetting> findByFacultyId(Long facultyId);

    List<FacultyNotificationSetting> findByCollegeId(Long collegeId);
}
