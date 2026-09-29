package com.kuaima.app.admin.repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

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

    /** 端侧按发布范围查询已发布公告，最新公告优先。 */
    Page<Notice> findByStatusAndScopeInOrderByIdDesc(String status, Collection<String> scopes, Pageable pageable);

    /** 指定端可见的最新已发布公告。 */
    Optional<Notice> findFirstByStatusAndScopeInOrderByIdDesc(String status, Collection<String> scopes);
}
