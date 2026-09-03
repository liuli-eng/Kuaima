package com.suma.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.admin.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
