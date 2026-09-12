package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.MessageTemplate;
import com.kuaima.app.admin.repository.MessageTemplateRepository;
import com.kuaima.app.common.Result;

/** 消息模板管理 CRUD */
@RestController
@RequestMapping("/admin/message-templates")
@Tag(name = "后台-消息模板", description = "消息模板管理")
public class AdminMessageTemplateController {

    private final MessageTemplateRepository repo;

    public AdminMessageTemplateController(MessageTemplateRepository repo) { this.repo = repo; }

    /** 列表（分页）：GET /admin/message-templates?status=&event=&page=&size= */
    @Operation(summary = "消息模板列表", description = "分页查询。参数：status(可选)、event(可选)、page(默认0)、size(默认10)，按 updateTime 倒序；status 优先于 event")
    @GetMapping
    public Result<List<MessageTemplate>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String event,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<MessageTemplate> result;
        if (status != null && !status.isEmpty()) {
            result = repo.findByStatus(status, pageable);
        } else if (event != null && !event.isEmpty()) {
            result = repo.findByEvent(event, pageable);
        } else {
            result = repo.findAll(pageable);
        }
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

    @Operation(summary = "消息模板详情", description = "按 id 查询模板；不存在抛出异常")
    @GetMapping("/{id}")
    public Result<MessageTemplate> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @Operation(summary = "创建消息模板", description = "body 为 MessageTemplate 字段；自动填充 createTime/updateTime；缺省 status=enabled、channel=both、sendWay=即时、sendTime=全天、freqLimit=5；非定时方式清空 scheduledTime")
    @PostMapping
    public Result<MessageTemplate> create(@RequestBody MessageTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        template.setCreateTime(now);
        template.setUpdateTime(now);
        if (template.getStatus() == null || template.getStatus().isEmpty()) {
            template.setStatus("enabled");
        }
        if (template.getChannel() == null || template.getChannel().isEmpty()) {
            template.setChannel("both");
        }
        if (template.getSendWay() == null || template.getSendWay().isEmpty()) {
            template.setSendWay("即时");
        }
        // 非定时方式清空定时时间
        if (!"定时".equals(template.getSendWay())) {
            template.setScheduledTime(null);
        }
        if (template.getSendTime() == null || template.getSendTime().isEmpty()) {
            template.setSendTime("全天");
        }
        if (template.getFreqLimit() == null || template.getFreqLimit().isEmpty()) {
            template.setFreqLimit("5");
        }
        return Result.success(repo.save(template));
    }

    @Operation(summary = "更新消息模板", description = "按 id 局部更新（字段非空才覆盖）；自动刷新 updateTime；模板不存在抛出异常")
    @PutMapping("/{id}")
    public Result<MessageTemplate> update(@PathVariable Long id, @RequestBody MessageTemplate template) {
        MessageTemplate existing = repo.findById(id).orElseThrow();
        if (template.getName() != null) existing.setName(template.getName());
        if (template.getEvent() != null) existing.setEvent(template.getEvent());
        if (template.getScene() != null) existing.setScene(template.getScene());
        if (template.getChannel() != null) existing.setChannel(template.getChannel());
        if (template.getContent() != null) existing.setContent(template.getContent());
        if (template.getStatus() != null) existing.setStatus(template.getStatus());
        if (template.getSendWay() != null) existing.setSendWay(template.getSendWay());
        if (template.getScheduledTime() != null) existing.setScheduledTime(template.getScheduledTime());
        if (template.getSendTime() != null) existing.setSendTime(template.getSendTime());
        if (template.getFreqLimit() != null) existing.setFreqLimit(template.getFreqLimit());
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    /** 启用/禁用：PUT /admin/message-templates/{id}/toggle */
    @Operation(summary = "启用/禁用模板", description = "切换状态：enabled<->disabled；自动刷新 updateTime；模板不存在抛出异常")
    @PutMapping("/{id}/toggle")
    public Result<MessageTemplate> toggle(@PathVariable Long id) {
        MessageTemplate existing = repo.findById(id).orElseThrow();
        existing.setStatus("enabled".equals(existing.getStatus()) ? "disabled" : "enabled");
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    /** 标记最近使用：PUT /admin/message-templates/{id}/used */
    @Operation(summary = "标记最近使用", description = "更新 lastUsed 为当前时间；模板不存在抛出异常")
    @PutMapping("/{id}/used")
    public Result<MessageTemplate> markUsed(@PathVariable Long id) {
        MessageTemplate existing = repo.findById(id).orElseThrow();
        existing.setLastUsed(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @Operation(summary = "删除消息模板", description = "按 id 删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}
