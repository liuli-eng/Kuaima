package com.kuaima.app.controller.resume;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.resume.entity.Resume;
import com.kuaima.app.domain.resume.entity.ResumeImportRecord;
import com.kuaima.app.domain.resume.service.ResumeService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/resumes")
@Tag(name = "老板-简历库", description = "简历库统计、列表筛选、详情、收藏、批量操作、简历导入")
public class ResumeController {

    private final ResumeService service;

    public ResumeController(ResumeService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    @Operation(summary = "简历库统计：总数/今日新增/待处理/已投递/收藏")
    public Result<Map<String, Object>> stats(Authentication auth) {
        return Result.success(service.stats(currentUserId(auth)));
    }

    @GetMapping("/latest")
    @Operation(summary = "最新简历（首页展示，最多5条）")
    public Result<List<Resume>> latest(Authentication auth) {
        return Result.success(service.latest(currentUserId(auth)));
    }

    @GetMapping
    @Operation(summary = "简历列表（tab: all/pending/viewed/sent/fav；支持关键词与筛选）")
    public Result<Page<Resume>> list(Authentication auth,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String jobCategory,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String expectedSalary,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(service.list(currentUserId(auth), tab, keyword, jobCategory,
                education, expectedSalary, status, page, size));
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "简历详情（含教育/工作/项目经历）")
    public Result<Map<String, Object>> detail(Authentication auth, @PathVariable Long resumeId) {
        return Result.success(service.detail(currentUserId(auth), resumeId));
    }

    @PutMapping("/{resumeId}/favorite")
    @Operation(summary = "收藏/取消收藏")
    public Result<Resume> toggleFavorite(Authentication auth, @PathVariable Long resumeId) {
        return Result.success(service.toggleFavorite(currentUserId(auth), resumeId));
    }

    @PutMapping("/{resumeId}/status")
    @Operation(summary = "更新简历状态（NEW/PENDING/VIEWED/SENT）")
    public Result<Resume> updateStatus(Authentication auth, @PathVariable Long resumeId,
            @RequestBody Map<String, String> body) {
        return Result.success(service.updateStatus(currentUserId(auth), resumeId,
                body.getOrDefault("status", "VIEWED")));
    }

    @DeleteMapping("/{resumeId}")
    @Operation(summary = "删除简历")
    public Result<Void> delete(Authentication auth, @PathVariable Long resumeId) {
        service.delete(currentUserId(auth), resumeId);
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作（action: read/fav/del/process）")
    public Result<Map<String, Object>> batch(Authentication auth, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> rawIds = (List<Number>) body.getOrDefault("ids", List.of());
        List<Long> ids = rawIds.stream().map(Number::longValue).toList();
        String action = String.valueOf(body.getOrDefault("action", ""));
        return Result.success(service.batch(currentUserId(auth), ids, action));
    }

    @PostMapping("/imports")
    @Operation(summary = "新增导入记录（fileName/fileUrl/success/failReason）")
    public Result<ResumeImportRecord> createImport(Authentication auth,
            @RequestBody Map<String, Object> body) {
        Long userId = currentUserId(auth);
        return Result.success(service.createImport(userId, null,
                String.valueOf(body.getOrDefault("fileName", "未命名文件")),
                body.get("fileUrl") != null ? String.valueOf(body.get("fileUrl")) : null,
                !Boolean.FALSE.equals(body.get("success")),
                body.get("failReason") != null ? String.valueOf(body.get("failReason")) : null));
    }

    @GetMapping("/imports")
    @Operation(summary = "导入记录列表")
    public Result<List<ResumeImportRecord>> listImports(Authentication auth) {
        return Result.success(service.listImports(currentUserId(auth)));
    }

    private Long currentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof LoginUser login && login.id() != null) {
            return login.id();
        }
        throw new com.kuaima.app.common.ForbiddenBusinessException("请先登录老板账号");
    }
}
