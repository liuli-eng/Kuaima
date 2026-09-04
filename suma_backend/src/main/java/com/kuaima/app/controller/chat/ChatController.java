package com.kuaima.app.controller.chat;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
 * 自动回复当前为占位文案，后续可对接客服坐席或机器人。
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
        ChatSession session = new ChatSession();
        session.setUserId(req.getUserId());
        session.setAgentId(1L); // 占位：固定分配
        session.setStatus("OPEN");
        return Result.success(chatSessionRepository.save(session));
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

    /** 发送消息：保存用户消息，并返回客服自动回复占位 */
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

        // 客服自动回复占位
        ChatMessage reply = new ChatMessage();
        reply.setSessionId(id);
        reply.setFromId(1L);
        reply.setFromType("AGENT");
        reply.setContent("客服正在为您服务，请稍候...");
        reply.setContentType("TEXT");
        chatMessageRepository.save(reply);

        return Result.success(saved);
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
