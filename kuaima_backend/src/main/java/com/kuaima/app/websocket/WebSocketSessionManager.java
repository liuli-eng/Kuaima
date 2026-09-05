package com.kuaima.app.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 会话管理器：维护 userId -> WebSocketSession 映射
 * 支持用户端(USER)和客服端(AGENT)双向消息推送
 */
@Component
public class WebSocketSessionManager {

    /** userId -> WebSocketSession */
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void addSession(String userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }

    public void removeSession(String userId) {
        userSessions.remove(userId);
    }

    public WebSocketSession getSession(String userId) {
        return userSessions.get(userId);
    }

    public boolean isOnline(String userId) {
        WebSocketSession s = userSessions.get(userId);
        return s != null && s.isOpen();
    }

    public int getOnlineCount() {
        return (int) userSessions.values().stream().filter(WebSocketSession::isOpen).count();
    }
}
