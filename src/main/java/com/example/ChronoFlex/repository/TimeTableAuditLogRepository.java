package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.TimeTableAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeTableAuditLogRepository extends JpaRepository<TimeTableAuditLog, Long> {
}
