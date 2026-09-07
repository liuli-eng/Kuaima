package com.kuaima.app.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 按状态过滤 + 分页 */
    Page<Notice> findByStatus(String status, Pageable pageable);
}
