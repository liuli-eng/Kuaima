package com.kuaima.app.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.AdminLog;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    /** 按操作类型过滤 + 分页 */
    Page<AdminLog> findByType(String type, Pageable pageable);

    /** 按操作人 ID 过滤 + 分页 */
    Page<AdminLog> findByOperatorId(Long operatorId, Pageable pageable);

    /** 按操作人 ID + 操作类型过滤 + 分页 */
    Page<AdminLog> findByOperatorIdAndType(Long operatorId, String type, Pageable pageable);
}
