package com.kuaima.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.AdminLog;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
