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
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Notice;
import com.kuaima.app.admin.repository.NoticeRepository;
import com.kuaima.app.common.Result;

/** 公告管理 CRUD */
@RestController
@RequestMapping("/admin/notices")
public class AdminNoticeController {

    private final NoticeRepository repo;

    public AdminNoticeController(NoticeRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<Notice>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/{id}")
    public Result<Notice> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @PostMapping
    public Result<Notice> create(@RequestBody Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        if ("已发布".equals(notice.getStatus())) {
            notice.setPublishTime(LocalDateTime.now());
        }
        return Result.success(repo.save(notice));
    }

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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}
