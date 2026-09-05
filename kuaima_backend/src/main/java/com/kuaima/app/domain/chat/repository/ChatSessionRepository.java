package com.kuaima.app.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.chat.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /** 某用户会话列表，按时间倒序（最近优先） */
    List<ChatSession> findByUserIdOrderByTimestampDesc(Long userId);

    /** 按状态查询会话列表，按时间倒序 */
    List<ChatSession> findByStatusOrderByTimestampDesc(String status);

    /** 统计某状态的会话数 */
    long countByStatus(String status);
}
