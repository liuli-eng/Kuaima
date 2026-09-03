package com.suma.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.admin.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
