package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.TimeTableBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeTableBackupRepository extends JpaRepository<TimeTableBackup, Long> {

    // Fetch backups for a college + semester + section (most recent first)
    List<TimeTableBackup> findByCollegeIdAndSemesterAndSectionOrderByBackedUpAtDesc(Long collegeId, String semester, String section);

    // Fetch all backups for a college (optionally to show history)
    List<TimeTableBackup> findByCollegeIdOrderByBackedUpAtDesc(Long collegeId);


}
