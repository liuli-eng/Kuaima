package com.kuaima.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
