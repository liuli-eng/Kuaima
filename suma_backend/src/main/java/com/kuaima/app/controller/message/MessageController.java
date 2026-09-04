package com.kuaima.app.controller.message;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class MessageController {

    private final MessageService messageService;

    /** 未读消息数（tab 红点角标） */
    @GetMapping("/unread")
    public Result<Long> unread(@RequestParam Long userId) {
        return Result.success(messageService.unreadCount(userId));
    }

    /** 消息列表（分页，page 从 0 开始），read 传 true/false 可只看已读/未读 */
    @GetMapping("/list")
    public Result<List<Message>> list(@RequestParam Long userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Message> result = messageService.list(userId, read, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 单条标记已读 */
    @PutMapping("/{id}/read")
    public Result<Boolean> read(@PathVariable Long id, @RequestParam Long userId) {
        if (!messageService.markRead(userId, id)) {
            return Result.error(404, "消息不存在或不属于该用户");
        }
        return Result.success(true);
    }

    /** 消息详情（单条按 id 查询，校验归属） */
    @GetMapping("/{id}")
    public Result<Message> detail(@PathVariable Long id, @RequestParam Long userId) {
        Message message = messageService.getById(userId, id);
        if (message == null) {
            return Result.error(404, "消息不存在或不属于该用户");
        }
        return Result.success(message);
    }

    /** 系统通知列表（按 type=SYSTEM_NOTICE 过滤分页） */
    @GetMapping("/system")
    public Result<List<Message>> system(@RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Message> result = messageService.listByType(userId, "SYSTEM_NOTICE", page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 全部标记已读 */
    @PutMapping("/readAll")
    public Result<Integer> readAll(@RequestParam Long userId) {
        return Result.success(messageService.markAllRead(userId));
    }
}
