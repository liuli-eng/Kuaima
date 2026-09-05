package com.kuaima.app.websocket;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.domain.chat.entity.ChatMessage;
import com.kuaima.app.domain.chat.entity.ChatSession;
import com.kuaima.app.domain.chat.repository.ChatMessageRepository;
import com.kuaima.app.domain.chat.repository.ChatSessionRepository;

import lombok.RequiredArgsConstructor;

/**
 * 客服聊天 WebSocket Handler
 * 支持消息类型: JOIN / MESSAGE / TYPING / READ / CLOSE
 */
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String type = (String) session.getAttributes().get("type");
        if (userId != null && !userId.isEmpty()) {
            sessionManager.addSession(userId, session);
            System.out.println("[WebSocket] 连接建立: userId=" + userId + ", type=" + type);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject msg;
        try {
            msg = JSON.parseObject(payload);
        } catch (Exception e) {
            sendError(session, "消息格式错误");
            return;
        }

        String type = msg.getString("type");
        if (type == null) {
            sendError(session, "缺少 type 字段");
            return;
        }

        String userId = (String) session.getAttributes().get("userId");
        String fromType = (String) session.getAttributes().get("type");

        switch (type.toUpperCase()) {
            case "PING" -> { /* 心跳，静默处理 */ }
            case "JOIN" -> handleJoin(session, msg, userId, fromType);
            case "MESSAGE" -> handleMessage(session, msg, userId, fromType);
            case "TYPING" -> handleTyping(session, msg, userId, fromType);
            case "READ" -> handleRead(session, msg, userId, fromType);
            case "CLOSE" -> handleClose(session, msg, userId, fromType);
            default -> sendError(session, "未知消息类型: " + type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId);
            System.out.println("[WebSocket] 连接关闭: userId=" + userId);
        }
    }

    // ========== 消息处理 ==========

    private void handleJoin(WebSocketSession session, JSONObject msg, String userId, String fromType) {
        Long sessionId = msg.getLong("sessionId");
        if (sessionId == null) {
            sendError(session, "缺少 sessionId");
            return;
        }
        // 广播 AGENT_JOINED
        JSONObject resp = new JSONObject();
        resp.put("type", "AGENT_JOINED");
        resp.put("sessionId", sessionId);
        resp.put("timestamp", System.currentTimeMillis());
        sendToSession(session, resp.toJSONString());
    }

    private void handleMessage(WebSocketSession session, JSONObject msg, String userId, String fromType) {
        Long sessionId = msg.getLong("sessionId");
        String content = msg.getString("content");
        String contentType = msg.getString("contentType");
        if (contentType == null) contentType = "TEXT";
        if (sessionId == null || content == null) {
            sendError(session, "缺少 sessionId 或 content");
            return;
        }

        // 持久化消息
        ChatMessage chatMsg = new ChatMessage();
        chatMsg.setSessionId(sessionId);
        chatMsg.setFromId(Long.parseLong(userId));
        chatMsg.setFromType(fromType);
        chatMsg.setContent(content);
        chatMsg.setContentType(contentType);
        ChatMessage saved = chatMessageRepository.save(chatMsg);

        // 更新会话时间
        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            s.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
            chatSessionRepository.save(s);
        });

        // 构造推送消息
        JSONObject push = new JSONObject();
        push.put("type", "MESSAGE");
        push.put("sessionId", sessionId);
        push.put("messageId", saved.getId());
        push.put("content", content);
        push.put("fromType", fromType);
        push.put("fromId", userId);
        push.put("contentType", contentType);
        push.put("timestamp", System.currentTimeMillis());

        // 推送给发送者自己
        sendToSession(session, push.toJSONString());

        // 推送给对方
        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            String targetId = "USER".equalsIgnoreCase(fromType)
                    ? String.valueOf(s.getAgentId())
                    : String.valueOf(s.getUserId());
            WebSocketSession target = sessionManager.getSession(targetId);
            if (target != null && target.isOpen()) {
                sendToSession(target, push.toJSONString());
            }
        });
    }

    private void handleTyping(WebSocketSession session, JSONObject msg, String userId, String fromType) {
        Long sessionId = msg.getLong("sessionId");
        if (sessionId == null) return;

        JSONObject push = new JSONObject();
        push.put("type", "TYPING");
        push.put("sessionId", sessionId);
        push.put("fromType", fromType);
        push.put("timestamp", System.currentTimeMillis());

        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            String targetId = "USER".equalsIgnoreCase(fromType)
                    ? String.valueOf(s.getAgentId())
                    : String.valueOf(s.getUserId());
            WebSocketSession target = sessionManager.getSession(targetId);
            if (target != null && target.isOpen()) {
                sendToSession(target, push.toJSONString());
            }
        });
    }

    private void handleRead(WebSocketSession session, JSONObject msg, String userId, String fromType) {
        Long sessionId = msg.getLong("sessionId");
        if (sessionId == null) return;

        JSONObject push = new JSONObject();
        push.put("type", "READ");
        push.put("sessionId", sessionId);
        push.put("fromType", fromType);
        push.put("timestamp", System.currentTimeMillis());

        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            String targetId = "USER".equalsIgnoreCase(fromType)
                    ? String.valueOf(s.getAgentId())
                    : String.valueOf(s.getUserId());
            WebSocketSession target = sessionManager.getSession(targetId);
            if (target != null && target.isOpen()) {
                sendToSession(target, push.toJSONString());
            }
        });
    }

    private void handleClose(WebSocketSession session, JSONObject msg, String userId, String fromType) {
        Long sessionId = msg.getLong("sessionId");
        if (sessionId == null) return;

        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            s.setStatus("CLOSED");
            chatSessionRepository.save(s);
        });

        JSONObject push = new JSONObject();
        push.put("type", "SESSION_CLOSED");
        push.put("sessionId", sessionId);
        push.put("timestamp", System.currentTimeMillis());

        // 通知双方
        sendToSession(session, push.toJSONString());
        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            String targetId = "USER".equalsIgnoreCase(fromType)
                    ? String.valueOf(s.getAgentId())
                    : String.valueOf(s.getUserId());
            WebSocketSession target = sessionManager.getSession(targetId);
            if (target != null && target.isOpen()) {
                sendToSession(target, push.toJSONString());
            }
        });
    }

    // ========== 工具方法 ==========

    private void sendError(WebSocketSession session, String error) {
        JSONObject err = new JSONObject();
        err.put("type", "ERROR");
        err.put("message", error);
        sendToSession(session, err.toJSONString());
    }

    private void sendToSession(WebSocketSession session, String text) {
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            System.err.println("[WebSocket] 发送消息失败: " + e.getMessage());
        }
    }
}
