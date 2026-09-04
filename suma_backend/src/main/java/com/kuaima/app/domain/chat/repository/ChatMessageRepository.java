package com.kuaima.app.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** 某会话所有消息，按时间正序（早→晚，便于前端顺序渲染） */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
}
