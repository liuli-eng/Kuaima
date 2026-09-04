package com.kuaima.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
