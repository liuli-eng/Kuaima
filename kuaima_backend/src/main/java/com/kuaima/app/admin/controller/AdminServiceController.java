package com.kuaima.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.QuickReply;
import com.kuaima.app.admin.repository.QuickReplyRepository;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.chat.entity.ChatMessage;
import com.kuaima.app.domain.chat.entity.ChatSession;
import com.kuaima.app.domain.chat.repository.ChatMessageRepository;
import com.kuaima.app.domain.chat.repository.ChatSessionRepository;
import com.kuaima.app.domain.faq.entity.Faq;
import com.kuaima.app.domain.faq.repository.FaqRepository;

import lombok.RequiredArgsConstructor;

/**
 * 后台客服管理 Controller
 * 提供会话管理、快捷回复、FAQ 管理等接口
 */
@RestController
@RequestMapping("/admin/service")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final QuickReplyRepository quickReplyRepository;
    private final FaqRepository faqRepository;

    // ==================== 客服统计 ====================

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("openSessions", chatSessionRepository.countByStatus("OPEN"));
        stats.put("closedSessions", chatSessionRepository.countByStatus("CLOSED"));
        stats.put("totalQuickReplies", quickReplyRepository.count());
        stats.put("totalFaqs", faqRepository.count());
        return Result.success(stats);
    }

    // ==================== 会话管理 ====================

    @GetMapping("/sessions")
    public Result<List<ChatSession>> sessions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ChatSession> all = (status != null && !status.isEmpty())
                ? chatSessionRepository.findByStatusOrderByTimestampDesc(status)
                : chatSessionRepository.findAll();
        // 排序
        all.sort((a, b) -> {
            if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        return Result.success(all.subList(from, to), page, total);
    }

    @GetMapping("/sessions/{id}")
    public Result<ChatSession> sessionDetail(@PathVariable Long id) {
        return chatSessionRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error("会话不存在"));
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<ChatMessage>> sessionMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<ChatMessage> all = chatMessageRepository.findBySessionIdOrderByTimestampAsc(id);
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        return Result.success(all.subList(from, to), page, total);
    }

    @PostMapping("/sessions/{id}/messages")
    public Result<ChatMessage> sendMessage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        if (content == null || content.isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(id);
        msg.setFromId(1L); // TODO: 从登录上下文获取
        msg.setFromType("AGENT");
        msg.setContent(content);
        msg.setContentType("TEXT");
        ChatMessage saved = chatMessageRepository.save(msg);

        // 更新会话时间
        chatSessionRepository.findById(id).ifPresent(s -> {
            s.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
            chatSessionRepository.save(s);
        });
        return Result.success(saved);
    }

    @PutMapping("/sessions/{id}/close")
    public Result<ChatSession> closeSession(@PathVariable Long id) {
        return chatSessionRepository.findById(id).map(s -> {
            s.setStatus("CLOSED");
            return Result.success(chatSessionRepository.save(s));
        }).orElse(Result.error("会话不存在"));
    }

    // ==================== 快捷回复管理 ====================

    @GetMapping("/quick-replies")
    public Result<List<QuickReply>> quickReplies() {
        return Result.success(quickReplyRepository.findAll());
    }

    @PostMapping("/quick-replies")
    public Result<QuickReply> createQuickReply(@RequestBody QuickReply reply) {
        if (reply.getEnabled() == null) reply.setEnabled(true);
        if (reply.getSortOrder() == null) reply.setSortOrder(0);
        return Result.success(quickReplyRepository.save(reply));
    }

    @PutMapping("/quick-replies/{id}")
    public Result<QuickReply> updateQuickReply(@PathVariable Long id, @RequestBody QuickReply reply) {
        return quickReplyRepository.findById(id).map(existing -> {
            existing.setContent(reply.getContent());
            existing.setCategory(reply.getCategory());
            existing.setSortOrder(reply.getSortOrder());
            existing.setEnabled(reply.getEnabled());
            return Result.success(quickReplyRepository.save(existing));
        }).orElse(Result.error("快捷回复不存在"));
    }

    @DeleteMapping("/quick-replies/{id}")
    public Result<Void> deleteQuickReply(@PathVariable Long id) {
        quickReplyRepository.deleteById(id);
        return Result.success(null);
    }

    // ==================== FAQ 管理 ====================

    @GetMapping("/faqs")
    public Result<List<Faq>> faqs() {
        return Result.success(faqRepository.findAll());
    }

    @PostMapping("/faqs")
    public Result<Faq> createFaq(@RequestBody Faq faq) {
        if (faq.getEnabled() == null) faq.setEnabled(true);
        if (faq.getSortOrder() == null) faq.setSortOrder(0);
        return Result.success(faqRepository.save(faq));
    }

    @PutMapping("/faqs/{id}")
    public Result<Faq> updateFaq(@PathVariable Long id, @RequestBody Faq faq) {
        return faqRepository.findById(id).map(existing -> {
            existing.setQuestion(faq.getQuestion());
            existing.setAnswer(faq.getAnswer());
            existing.setCategory(faq.getCategory());
            existing.setSortOrder(faq.getSortOrder());
            existing.setEnabled(faq.getEnabled());
            return Result.success(faqRepository.save(existing));
        }).orElse(Result.error("FAQ不存在"));
    }

    @DeleteMapping("/faqs/{id}")
    public Result<Void> deleteFaq(@PathVariable Long id) {
        faqRepository.deleteById(id);
        return Result.success(null);
    }
}
