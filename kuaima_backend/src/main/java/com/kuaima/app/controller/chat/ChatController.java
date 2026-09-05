package com.kuaima.app.controller.chat;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.chat.entity.ChatMessage;
import com.kuaima.app.domain.chat.entity.ChatSession;
import com.kuaima.app.domain.chat.repository.ChatMessageRepository;
import com.kuaima.app.domain.chat.repository.ChatSessionRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 在线客服：用户发起会话 -> 自动分配客服 -> 多轮消息交互。
 * WebSocket 处理实时通信，HTTP 接口用于降级和历史查询。
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    /** 发起会话：自动分配客服（当前固定 agentId=1） */
    @PostMapping("/sessions")
    @Transactional
    public Result<ChatSession> startSession(@RequestBody StartSessionRequest req) {
        // 先查找是否已有未关闭的会话
        List<ChatSession> existing = chatSessionRepository.findByUserIdOrderByTimestampDesc(req.getUserId());
        for (ChatSession s : existing) {
            if ("OPEN".equals(s.getStatus())) {
                return Result.success(s);
            }
        }
        ChatSession session = new ChatSession();
        session.setUserId(req.getUserId());
        session.setAgentId(1L); // 占位：固定分配
        session.setStatus("OPEN");
        return Result.success(chatSessionRepository.save(session));
    }

    /** 获取用户会话列表 */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> userSessions(@RequestParam Long userId) {
        return Result.success(chatSessionRepository.findByUserIdOrderByTimestampDesc(userId));
    }

    /** 会话消息列表（分页，时间正序） */
    @GetMapping("/sessions/{id}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<ChatMessage> all = chatMessageRepository.findBySessionIdOrderByTimestampAsc(id);
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<ChatMessage> pageContent = all.subList(from, to);
        return Result.success(pageContent, page, total);
    }

    /** 发送消息（HTTP 降级，WebSocket 不可用时使用） */
    @PostMapping("/sessions/{id}/messages")
    @Transactional
    public Result<ChatMessage> sendMessage(@PathVariable Long id, @RequestBody SendMessageRequest req) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(id);
        userMsg.setFromId(req.getFromId());
        userMsg.setFromType("USER");
        userMsg.setContent(req.getContent());
        userMsg.setContentType("TEXT");
        ChatMessage saved = chatMessageRepository.save(userMsg);

        // 更新会话时间
        chatSessionRepository.findById(id).ifPresent(s -> {
            s.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
            chatSessionRepository.save(s);
        });

        return Result.success(saved);
    }

    /** 关闭会话 */
    @PutMapping("/sessions/{id}/close")
    @Transactional
    public Result<ChatSession> closeSession(@PathVariable Long id) {
        return chatSessionRepository.findById(id).map(s -> {
            s.setStatus("CLOSED");
            return Result.success(chatSessionRepository.save(s));
        }).orElse(Result.error("会话不存在"));
    }

    @Getter
    @Setter
    public static class StartSessionRequest {
        private Long userId;
    }

    @Getter
    @Setter
    public static class SendMessageRequest {
        private Long fromId;
        private String content;
    }
}
