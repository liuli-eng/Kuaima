package com.kuaima.app.domain.message.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.message.entity.Message;
import com.kuaima.app.domain.message.repository.MessageRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 站内消息中心：事件写入收件箱，支撑 未读数/列表/已读 接口（前端轮询）。
 * 消息写入与业务同事务，目标用户不存在时静默跳过，不影响主流程。
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /** 发送给单个用户（老板/零工均可），目标用户不存在时跳过并返回 null */
    @Transactional
    public Message sendToUser(Long userId, String type, String title, String content,
            String bizType, Long bizId) {
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        return messageRepository.save(build(user.getId(), user.getRole(), type, title, content, bizType, bizId));
    }

    /** 发给一组用户（已按 userId 去重），用户不存在自动跳过 */
    @Transactional
    public void sendToList(List<Long> userIds, String type, String title, String content,
            String bizType, Long bizId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> distinctIds = userIds.stream().distinct().collect(Collectors.toList());
        List<Message> messages = distinctIds.stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(u -> u != null)
                .map(u -> build(u.getId(), u.getRole(), type, title, content, bizType, bizId))
                .toList();
        if (!messages.isEmpty()) {
            messageRepository.saveAll(messages);
        }
    }

    /** 广播给全部员工(USER)：岗位发布视为招聘广播（量大后可按城市/类型定向） */
    @Transactional
    public void broadcastToUsers(String type, String title, String content, String bizType, Long bizId) {
        List<User> users = userRepository.findByRole(UserRole.USER);
        if (users.isEmpty()) {
            return;
        }
        List<Message> messages = users.stream()
                .map(u -> build(u.getId(), UserRole.USER, type, title, content, bizType, bizId))
                .toList();
        messageRepository.saveAll(messages);
    }

    /** 未读消息数 */
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return messageRepository.countByUserIdAndReadFlagFalse(userId);
    }

    /** 消息列表分页（page 从 0 开始，与 /boss/order 一致）；read 为空表示不分已读/未读 */
    @Transactional(readOnly = true)
    public Page<Message> list(Long userId, Boolean read, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        if (read == null) {
            return messageRepository.findByUserIdOrderByIdDesc(userId, pageable);
        }
        return messageRepository.findByUserIdAndReadFlagOrderByIdDesc(userId, read, pageable);
    }

    /** 系统通知列表分页（按 type 过滤，如 SYSTEM_NOTICE） */
    @Transactional(readOnly = true)
    public Page<Message> listByType(Long userId, String type, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        return messageRepository.findByUserIdAndTypeOrderByIdDesc(userId, type, pageable);
    }

    /** 消息详情：校验归属，不属于该用户返回 null */
    @Transactional(readOnly = true)
    public Message getById(Long userId, Long messageId) {
        return messageRepository.findByIdAndUserId(messageId, userId).orElse(null);
    }

    /** 单条标记已读，校验归属，消息不存在或不属于该用户返回 false */
    @Transactional
    public boolean markRead(Long userId, Long messageId) {
        Message message = messageRepository.findByIdAndUserId(messageId, userId).orElse(null);
        if (message == null || Boolean.TRUE.equals(message.getReadFlag())) {
            return message != null;
        }
        message.setReadFlag(true);
        message.setReadTime(LocalDateTime.now());
        messageRepository.save(message);
        return true;
    }

    /** 全部标记已读，返回本次标记条数 */
    @Transactional
    public int markAllRead(Long userId) {
        return messageRepository.markAllRead(userId, LocalDateTime.now());
    }

    private Message build(Long userId, String role, String type, String title, String content,
            String bizType, Long bizId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setRole(role);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setBizType(bizType);
        message.setBizId(bizId);
        message.setReadFlag(false);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}
