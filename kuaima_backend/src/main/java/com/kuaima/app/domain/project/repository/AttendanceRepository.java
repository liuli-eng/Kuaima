package com.kuaima.app.domain.project.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.project.entity.AttendanceRecord;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long>,
        JpaSpecificationExecutor<AttendanceRecord> {

    List<AttendanceRecord> findByProjectIdAndAttendDate(Long projectId, LocalDate attendDate);

    List<AttendanceRecord> findByProjectIdAndAttendDateBetween(Long projectId, LocalDate start, LocalDate end);

    List<AttendanceRecord> findTop10ByNameOrderByAttendDateDesc(String name);
}
