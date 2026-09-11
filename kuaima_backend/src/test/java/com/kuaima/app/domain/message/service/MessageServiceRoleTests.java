package com.kuaima.app.domain.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.entity.Message;
import com.kuaima.app.domain.message.repository.MessageRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.admin.entity.MessageTemplate;
import com.kuaima.app.admin.repository.MessageTemplateRepository;

class MessageServiceRoleTests {

    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private MessageService service;
    private MessageTemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        userRepository = mock(UserRepository.class);
        templateRepository = mock(MessageTemplateRepository.class);
        service = new MessageService(messageRepository, userRepository, templateRepository);
    }

    @Test
    void sendToUser_shouldKeepExplicitBossRoleAfterAccountSwitchesToUser() {
        User account = new User();
        account.setId(30L);
        account.setRole(UserRole.USER);
        when(userRepository.findById(30L)).thenReturn(Optional.of(account));
        when(messageRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.sendToUser(30L, UserRole.BOSS, MessageType.ORDER_APPLY,
                "有新的报名待审核", "报名内容", "item", 23L);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals(UserRole.BOSS, captor.getValue().getRole());
        assertEquals(MessageType.ORDER_APPLY, captor.getValue().getType());
    }

    @Test
    void sendToUser_shouldKeepExplicitUserRoleAfterAccountSwitchesToBoss() {
        User account = new User();
        account.setId(30L);
        account.setRole(UserRole.BOSS);
        when(userRepository.findById(30L)).thenReturn(Optional.of(account));
        when(messageRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.sendToUser(30L, UserRole.USER, MessageType.ORDER_HIRE,
                "录用通知", "录用内容", "item", 23L);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals(UserRole.USER, captor.getValue().getRole());
        assertEquals(MessageType.ORDER_HIRE, captor.getValue().getType());
    }

    @Test
    void userList_shouldUseEffectiveRoleQueryThatExcludesBossMessageTypes() {
        service.list(30L, UserRole.USER, null, 0, 20);

        verify(messageRepository).findByUserIdAndEffectiveRole(
                eq(30L), eq(UserRole.USER), eq(MessageType.BOSS_MESSAGE_TYPES),
                eq(MessageType.USER_MESSAGE_TYPES),
                org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void sendToUser_shouldUseEnabledEventTemplateAndRenderVariables() {
        User account = new User(); account.setId(30L); account.setRole(UserRole.USER);
        when(userRepository.findById(30L)).thenReturn(Optional.of(account));
        MessageTemplate template = new MessageTemplate(); template.setName("录用成功"); template.setContent("您已被{orderTitle}录用，工资{salary}元");
        when(templateRepository.findFirstByEventAndStatusOrderByUpdateTimeDesc("order_hire", "enabled")).thenReturn(Optional.of(template));
        when(messageRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendToUser(30L, UserRole.USER, MessageType.ORDER_HIRE, "旧标题", "旧正文", "item", 23L,
                Map.of("orderTitle", "搬运", "salary", 200));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class); verify(messageRepository).save(captor.capture());
        assertEquals("录用成功", captor.getValue().getTitle());
        assertEquals("您已被搬运录用，工资200元", captor.getValue().getContent());
    }
}
