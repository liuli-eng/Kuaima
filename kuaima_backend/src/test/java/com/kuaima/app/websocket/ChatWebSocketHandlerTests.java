package com.kuaima.app.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.kuaima.app.domain.chat.entity.ChatMessage;
import com.kuaima.app.domain.chat.entity.ChatSession;
import com.kuaima.app.domain.chat.repository.ChatMessageRepository;
import com.kuaima.app.domain.chat.repository.ChatSessionRepository;

import static org.mockito.Mockito.mock;

class ChatWebSocketHandlerTests {

    @Test
    void agentMessageShouldNotBeEchoedBackToSender() throws Exception {
        WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
        ChatSessionRepository sessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository messageRepository = mock(ChatMessageRepository.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(sessionManager, sessionRepository, messageRepository);
        WebSocketSession sender = mock(WebSocketSession.class);
        WebSocketSession user = mock(WebSocketSession.class);

        when(sender.getAttributes()).thenReturn(Map.of("userId", "1", "type", "AGENT"));
        when(sender.isOpen()).thenReturn(true);
        when(user.isOpen()).thenReturn(true);
        when(sessionManager.getSession("2001")).thenReturn(user);
        ChatSession chatSession = new ChatSession();
        chatSession.setUserId(2001L);
        chatSession.setAgentId(1L);
        when(sessionRepository.findById(2L)).thenReturn(java.util.Optional.of(chatSession));
        ChatMessage saved = new ChatMessage();
        saved.setId(10L);
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        new TestableChatWebSocketHandler(handler).receive(sender,
                new TextMessage("{\"type\":\"MESSAGE\",\"sessionId\":2,\"content\":\"您好\",\"contentType\":\"TEXT\"}"));

        verify(messageRepository).save(any(ChatMessage.class));
        verify(sender, never()).sendMessage(any(TextMessage.class));
        verify(user).sendMessage(any(TextMessage.class));
    }

    private static final class TestableChatWebSocketHandler {
        private final ChatWebSocketHandler delegate;

        private TestableChatWebSocketHandler(ChatWebSocketHandler delegate) {
            this.delegate = delegate;
        }

        private void receive(WebSocketSession session, TextMessage message) throws Exception {
            delegate.handleTextMessage(session, message);
        }
    }
}
