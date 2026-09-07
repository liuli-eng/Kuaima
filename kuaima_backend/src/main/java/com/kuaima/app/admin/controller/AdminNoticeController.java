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

import com.kuaima.app.admin.entity.Notice;
import com.kuaima.app.admin.repository.NoticeRepository;
import com.kuaima.app.common.Result;
import com.kuaima.app.security.model.LoginUser;

import org.springframework.security.core.context.SecurityContextHolder;

/** 公告管理 CRUD */
@RestController
@RequestMapping("/admin/notices")
@Tag(name = "后台-公告", description = "规则与公告管理")
public class AdminNoticeController {

    private final NoticeRepository repo;

    public AdminNoticeController(NoticeRepository repo) { this.repo = repo; }

    /** 获取当前登录管理员的显示名（优先姓名，其次账号） */
    private String currentAdminName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser u) {
            return u.username();
        }
        return null;
    }

    /** 列表（分页，按状态过滤，按 id 倒序） */
    @Operation(summary = "公告列表分页", description = "按 status(已发布/草稿/已下架) 过滤，按 id 倒序分页返回 Notice 列表")
    @GetMapping
    public Result<List<Notice>> list(@RequestParam(required = false) String status,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Notice> result = (status != null && !status.isEmpty())
                ? repo.findByStatus(status, pageable)
                : repo.findAll(pageable);
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

    @Operation(summary = "公告详情", description = "按 id 查询 Notice 完整信息")
    @GetMapping("/{id}")
    public Result<Notice> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @Operation(summary = "新增公告", description = "创建 Notice，自动记录发布人为当前登录管理员；新建/更新为「已发布」时自动记录 publishTime（仅首次）")
    @PostMapping
    public Result<Notice> create(@RequestBody Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        if (notice.getPublisher() == null || notice.getPublisher().isBlank()) {
            notice.setPublisher(currentAdminName());
        }
        if ("已发布".equals(notice.getStatus())) {
            notice.setPublishTime(LocalDateTime.now());
        }
        return Result.success(repo.save(notice));
    }

    @Operation(summary = "更新公告", description = "按 id 更新 Notice，字段非空才更新；首次置为「已发布」时自动记录 publishTime")
    @PutMapping("/{id}")
    public Result<Notice> update(@PathVariable Long id, @RequestBody Notice notice) {
        Notice existing = repo.findById(id).orElseThrow();
        if (notice.getTitle() != null) existing.setTitle(notice.getTitle());
        if (notice.getType() != null) existing.setType(notice.getType());
        if (notice.getScope() != null) existing.setScope(notice.getScope());
        if (notice.getContent() != null) existing.setContent(notice.getContent());
        if (notice.getStatus() != null) {
            existing.setStatus(notice.getStatus());
            if ("已发布".equals(notice.getStatus()) && existing.getPublishTime() == null) {
                existing.setPublishTime(LocalDateTime.now());
            }
        }
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @Operation(summary = "删除公告", description = "按 id 删除 Notice")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}
