package com.kuaima.app.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.AdminLog;
import com.kuaima.app.admin.repository.AdminLogRepository;
import com.kuaima.app.common.Result;
import com.kuaima.app.security.model.LoginUser;

/** 操作日志查询（只读） */
@RestController
@RequestMapping("/admin/logs")
@Tag(name = "后台-操作日志", description = "管理员操作日志查询")
public class AdminLogController {

    private final AdminLogRepository repo;

    public AdminLogController(AdminLogRepository repo) { this.repo = repo; }

    /** 获取当前登录管理员 */
    private LoginUser currentLoginUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof LoginUser u ? u : null;
    }

    @Operation(summary = "操作日志列表", description = "仅超级管理员可查看全部日志；管理员及其他角色仅可查看本人操作日志")
    @GetMapping
    public Result<Page<AdminLog>> list(@RequestParam(required = false) String type,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        LoginUser u = currentLoginUser();
        // 仅超级管理员可查看全部；其他角色（含普通管理员）仅查看本人日志
        // JWT 中角色带 ADMIN_ 前缀：ADMIN_SUPER_ADMIN / ADMIN_ADMIN / ADMIN_EDITOR / ADMIN_VIEWER
        boolean isSuperAdmin = u != null && u.role() != null && u.role().endsWith("SUPER_ADMIN");

        Page<AdminLog> result;
        if (isSuperAdmin) {
            result = (type != null && !type.isEmpty())
                    ? repo.findByType(type, pageable)
                    : repo.findAll(pageable);
        } else {
            Long operatorId = u != null ? u.id() : 0L;
            result = (type != null && !type.isEmpty())
                    ? repo.findByOperatorIdAndType(operatorId, type, pageable)
                    : repo.findByOperatorId(operatorId, pageable);
        }
        return Result.success(result, page, result.getTotalElements());
    }
}
