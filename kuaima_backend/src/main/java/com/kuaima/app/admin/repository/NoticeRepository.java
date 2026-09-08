package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 按状态过滤 + 分页 */
    Page<Notice> findByStatus(String status, Pageable pageable);

    /** 按状态 + 类型过滤 + 分页（用于登录后浮层只展示系统公告） */
    Page<Notice> findByStatusAndType(String status, String type, Pageable pageable);

    /** 按状态过滤 + id 倒序的未分页列表（取前 N 条做未读计算） */
    List<Notice> findTop20ByStatusOrderByIdDesc(String status);
}
