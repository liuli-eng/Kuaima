package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.kuaima.app.admin.entity.NoticeRead;
import com.kuaima.app.admin.repository.NoticeReadRepository;
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
    private final NoticeReadRepository readRepo;

    public AdminNoticeController(NoticeRepository repo, NoticeReadRepository readRepo) {
        this.repo = repo;
        this.readRepo = readRepo;
    }

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

    /** 获取当前登录管理员的 ID */
    private Long currentAdminId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser u) {
            return u.id();
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
    @GetMapping("/{id:[0-9]+}")
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
    @PutMapping("/{id:[0-9]+}")
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
    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }

    /** 当前登录管理员未读的公告（发布范围为 web账号 或 全部），按 id 倒序，用于登录后浮层 */
    @Operation(summary = "未读公告", description = "查询当前管理员未读的已发布公告（发布范围为 web账号 或 全部），按 id 倒序返回")
    @GetMapping("/unread")
    public Result<List<Notice>> unread() {
        Long adminId = currentAdminId();
        if (adminId == null) {
            return Result.success(new java.util.ArrayList<>());
        }
        // 取最近的 20 条已发布公告，排除已读，按发布范围过滤（web账号 或 全部）
        List<Notice> recent = repo.findTop20ByStatusOrderByIdDesc("已发布");
        Set<Long> readIds = new HashSet<>(readRepo.findReadNoticeIds(adminId));
        List<Notice> unread = recent.stream()
                .filter(n -> "web账号".equals(n.getScope()) || "全部".equals(n.getScope()))
                .filter(n -> !readIds.contains(n.getId()))
                .collect(Collectors.toList());
        return Result.success(unread);
    }

    /** 标记某条公告为已读（幂等：已存在则更新 readTime） */
    @Operation(summary = "标记已读", description = "当前管理员将指定 id 公告标记为已读")
    @PostMapping("/{id:[0-9]+}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Long adminId = currentAdminId();
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        if (!repo.existsById(id)) {
            return Result.error(404, "公告不存在");
        }
        if (!readRepo.existsByAdminUserIdAndNoticeId(adminId, id)) {
            NoticeRead rec = new NoticeRead();
            rec.setAdminUserId(adminId);
            rec.setNoticeId(id);
            rec.setReadTime(LocalDateTime.now());
            readRepo.save(rec);
        }
        return Result.success();
    }
}
