package com.suma.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.admin.entity.AdminLog;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
