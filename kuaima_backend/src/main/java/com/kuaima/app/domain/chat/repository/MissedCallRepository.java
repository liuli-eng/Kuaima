package com.kuaima.app.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.chat.entity.MissedCall;

public interface MissedCallRepository extends JpaRepository<MissedCall, Long> {

    /** 某用户未接来电，按通话时间倒序分页 */
    Page<MissedCall> findByToUserIdOrderByCallTimeDesc(Long toUserId, Pageable pageable);

    /** 某用户未读未接来电数（tab 红点） */
    long countByToUserIdAndIsReadFalse(Long toUserId);
}
