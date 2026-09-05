package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

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
public class AdminMessageTemplateController {

    private final MessageTemplateRepository repo;

    public AdminMessageTemplateController(MessageTemplateRepository repo) { this.repo = repo; }

    /** 列表：GET /admin/message-templates?status=&event= */
    @GetMapping
    public Result<List<MessageTemplate>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String event) {
        List<MessageTemplate> result;
        if (status != null && !status.isEmpty()) {
            result = repo.findByStatusOrderByUpdateTimeDesc(status);
        } else if (event != null && !event.isEmpty()) {
            result = repo.findByEventOrderByUpdateTimeDesc(event);
        } else {
            result = repo.findAll();
        }
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<MessageTemplate> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

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
        if (template.getSendTime() == null || template.getSendTime().isEmpty()) {
            template.setSendTime("全天");
        }
        if (template.getFreqLimit() == null || template.getFreqLimit().isEmpty()) {
            template.setFreqLimit("5");
        }
        return Result.success(repo.save(template));
    }

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
        if (template.getSendTime() != null) existing.setSendTime(template.getSendTime());
        if (template.getFreqLimit() != null) existing.setFreqLimit(template.getFreqLimit());
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    /** 启用/禁用：PUT /admin/message-templates/{id}/toggle */
    @PutMapping("/{id}/toggle")
    public Result<MessageTemplate> toggle(@PathVariable Long id) {
        MessageTemplate existing = repo.findById(id).orElseThrow();
        existing.setStatus("enabled".equals(existing.getStatus()) ? "disabled" : "enabled");
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    /** 标记最近使用：PUT /admin/message-templates/{id}/used */
    @PutMapping("/{id}/used")
    public Result<MessageTemplate> markUsed(@PathVariable Long id) {
        MessageTemplate existing = repo.findById(id).orElseThrow();
        existing.setLastUsed(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}
