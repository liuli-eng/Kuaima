package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.admin.entity.NoticeRead;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, Long> {

    /** 某管理员已读的所有公告 ID */
    @Query("SELECT n.noticeId FROM NoticeRead n WHERE n.adminUserId = :adminUserId")
    List<Long> findReadNoticeIds(@Param("adminUserId") Long adminUserId);

    /** 是否已读 */
    boolean existsByAdminUserIdAndNoticeId(Long adminUserId, Long noticeId);
}
