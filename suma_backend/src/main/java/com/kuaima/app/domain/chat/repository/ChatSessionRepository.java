package com.kuaima.app.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.chat.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /** 某用户会话列表，按时间倒序（最近优先） */
    List<ChatSession> findByUserIdOrderByTimestampDesc(Long userId);
}
