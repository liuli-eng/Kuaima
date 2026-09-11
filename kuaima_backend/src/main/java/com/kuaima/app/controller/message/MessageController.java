package com.kuaima.app.controller.message;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.kuaima.app.security.model.LoginUser;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.message.entity.Message;
import com.kuaima.app.domain.message.service.MessageService;

import lombok.RequiredArgsConstructor;

/**
 * 站内消息中心（BOSS/USER 共用）：
 * 事件发生时写入收件箱，小程序轮询以下接口拉取。
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "站内消息", description = "系统消息、订单消息、聊天消息列表")
public class MessageController {

    private final MessageService messageService;

    /** 未读消息数（tab 红点角标） */
    @Operation(summary = "未读消息数", description = "返回当前用户的未读消息数（long），用于 tab 红点角标")
    @GetMapping("/unread")
    public Result<Long> unread(@RequestParam Long userId,
                               @RequestParam(required = false) String role, Authentication authentication) {
        return Result.success(messageService.unreadCount(requireCurrentUser(userId, authentication), normalizeRole(role)));
    }

    /** 消息列表（分页，page 从 0 开始），read 传 true/false 可只看已读/未读 */
    @Operation(summary = "消息列表分页", description = "read 可选 true/false 只看已读/未读，不传返回全部；page 从 0 开始(默认 0)，size 默认 20。返回统一分页结构，最新在前。Message 字段含 userId、role、type、title、content、bizType、bizId、readFlag、readTime、createTime")
    @GetMapping("/list")
    public Result<List<Message>> list(@RequestParam Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        userId = requireCurrentUser(userId, authentication);
        String normalizedRole = normalizeRole(role);
        Page<Message> result = type == null || type.isBlank()
                ? messageService.list(userId, normalizedRole, read, page, size)
                : messageService.listByType(userId, normalizedRole, type.trim().toUpperCase(), read, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return null;
        String normalized = role.trim().toUpperCase();
        if (!"BOSS".equals(normalized) && !"USER".equals(normalized)) {
            throw new IllegalArgumentException("role 只能是 BOSS 或 USER");
        }
        return normalized;
    }

    /** 单条标记已读 */
    @Operation(summary = "单条标记已读", description = "校验该消息归属当前用户后置为已读并记录已读时间。消息不存在或不属于该用户返回 404")
    @PutMapping("/{id}/read")
    public Result<Boolean> read(@PathVariable Long id, @RequestParam Long userId, Authentication authentication) {
        userId = requireCurrentUser(userId, authentication);
        if (!messageService.markRead(userId, id)) {
            return Result.error(404, "消息不存在或不属于该用户");
        }
        return Result.success(true);
    }

    /** 消息详情（单条按 id 查询，校验归属） */
    @Operation(summary = "消息详情", description = "按 id 查询单条 Message 完整信息，校验归属当前用户")
    @GetMapping("/{id}")
    public Result<Message> detail(@PathVariable Long id, @RequestParam Long userId, Authentication authentication) {
        userId = requireCurrentUser(userId, authentication);
        Message message = messageService.getById(userId, id);
        if (message == null) {
            return Result.error(404, "消息不存在或不属于该用户");
        }
        return Result.success(message);
    }

    /** 系统通知列表（按 type=SYSTEM_NOTICE 过滤分页） */
    @Operation(summary = "系统通知列表", description = "按 type=SYSTEM_NOTICE 过滤分页返回 Message 列表")
    @GetMapping("/system")
    public Result<List<Message>> system(@RequestParam Long userId,
            @RequestParam(defaultValue = "USER") String role,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        userId = requireCurrentUser(userId, authentication);
        Page<Message> result = messageService.listSystem(userId, normalizeRole(role), read, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 全部标记已读 */
    @Operation(summary = "全部标记已读", description = "将当前用户所有未读消息置为已读，返回本次标记条数")
    @PutMapping("/readAll")
    public Result<Integer> readAll(@RequestParam Long userId, Authentication authentication) {
        userId = requireCurrentUser(userId, authentication);
        return Result.success(messageService.markAllRead(userId));
    }

    private Long requireCurrentUser(Long requestedUserId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser) || loginUser.id() == null) {
            throw new IllegalArgumentException("无法获取当前登录用户");
        }
        if (requestedUserId != null && !loginUser.id().equals(requestedUserId)) {
            throw new com.kuaima.app.common.ForbiddenBusinessException("无权访问其他用户的消息");
        }
        return loginUser.id();
    }
}
