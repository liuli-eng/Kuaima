package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.AdminUser;
import com.kuaima.app.admin.repository.AdminUserRepository;
import com.kuaima.app.common.Result;
import com.kuaima.app.security.model.LoginUser;

/** 管理员账号管理 */
@RestController
@RequestMapping("/admin/admin-users")
@Tag(name = "后台-用户CRUD", description = "用户增删改查")
public class AdminUserCrudController {

    private final AdminUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AdminUserCrudController(AdminUserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    /** 获取当前登录管理员的 ID（来自 JWT 过滤器写入的 SecurityContext） */
    private Long currentAdminId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser u) {
            return u.id();
        }
        return null;
    }

    @Operation(summary = "管理员列表", description = "分页查询管理员账号列表，按 id 倒序")
    @GetMapping
    public Result<Page<AdminUser>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<AdminUser> result = repo.findAll(pageable);
        return Result.success(result, page, result.getTotalElements());
    }

    /** 管理员详情 */
    @Operation(summary = "管理员详情", description = "按 ID 获取管理员账号详情，包含角色、权限树、创建者等")
    @GetMapping("/{id}")
    public Result<AdminUser> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @Operation(summary = "新建管理员", description = "新建管理员账号；不允许新建超级管理员（role=SUPER_ADMIN）；createdBy 自动记录为当前登录管理员 ID")
    @PostMapping
    public Result<AdminUser> create(@RequestBody AdminUser admin) {
        // 禁止新建超级管理员
        if ("SUPER_ADMIN".equals(admin.getRole())) {
            return Result.error(403, "不允许新建超级管理员账号");
        }
        if (repo.existsByUsername(admin.getUsername())) {
            return Result.error(400, "账号已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreatedBy(currentAdminId());
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(admin));
    }

    @Operation(summary = "编辑管理员", description = "编辑管理员账号；超级管理员受系统保护不可修改角色/权限/状态；非自己创建的账号，仅可修改基本信息（姓名/电话/邮箱/备注/部门），不可修改角色、权限树、状态、密码")
    @PutMapping("/{id}")
    public Result<AdminUser> update(@PathVariable Long id, @RequestBody AdminUser patch) {
        AdminUser existing = repo.findById(id).orElseThrow();
        boolean isSuperAdmin = "SUPER_ADMIN".equals(existing.getRole());
        Long currentId = currentAdminId();
        // 是否为当前管理员自己创建的账号（可改角色/权限/状态）
        boolean isOwnCreated = currentId != null && currentId.equals(existing.getCreatedBy());

        // 超级管理员保护：不允许修改角色、权限、状态
        if (isSuperAdmin) {
            if (patch.getRole() != null && !patch.getRole().equals(existing.getRole())) {
                return Result.error(403, "超级管理员角色不可修改");
            }
            if (patch.getPermissions() != null && !patch.getPermissions().equals(existing.getPermissions())) {
                return Result.error(403, "超级管理员权限不可修改");
            }
            if (patch.getStatus() != null && !patch.getStatus().equals(existing.getStatus())) {
                return Result.error(403, "超级管理员账号不可禁用");
            }
        } else if (!isOwnCreated) {
            // 非自己创建的账号：只能编辑基本信息，不能改角色/权限/状态/密码
            if ((patch.getRole() != null && !patch.getRole().equals(existing.getRole()))
                    || (patch.getPermissions() != null && !patch.getPermissions().equals(existing.getPermissions()))
                    || (patch.getStatus() != null && !patch.getStatus().equals(existing.getStatus()))
                    || (patch.getPassword() != null && !patch.getPassword().isBlank())) {
                return Result.error(403, "只能编辑自己创建的账号的角色/权限/状态");
            }
        }

        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getRole() != null && !isSuperAdmin && isOwnCreated) existing.setRole(patch.getRole());
        if (patch.getDept() != null) existing.setDept(patch.getDept());
        if (patch.getPhone() != null) existing.setPhone(patch.getPhone());
        if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
        if (patch.getRemark() != null) existing.setRemark(patch.getRemark());
        if (patch.getPermissions() != null && !isSuperAdmin && isOwnCreated) existing.setPermissions(patch.getPermissions());
        if (patch.getStatus() != null && isOwnCreated) existing.setStatus(patch.getStatus());
        if (patch.getPassword() != null && !patch.getPassword().isBlank() && isOwnCreated) {
            existing.setPassword(passwordEncoder.encode(patch.getPassword()));
        }
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    /** 重置密码 */
    @Operation(summary = "重置密码", description = "重置管理员密码；只能重置自己创建的账号的密码")
    @PutMapping("/{id}/reset-password")
    public Result<AdminUser> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        AdminUser existing = repo.findById(id).orElseThrow();
        Long currentId = currentAdminId();
        boolean isOwnCreated = currentId != null && currentId.equals(existing.getCreatedBy());
        if (!isOwnCreated) {
            return Result.error(403, "只能重置自己创建的账号的密码");
        }
        String pwd = body.getOrDefault("newPassword", "admin123");
        existing.setPassword(passwordEncoder.encode(pwd));
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @Operation(summary = "删除管理员", description = "删除管理员账号；超级管理员不可删除；只能删除自己创建的账号")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AdminUser existing = repo.findById(id).orElseThrow();
        if ("SUPER_ADMIN".equals(existing.getRole())) {
            return Result.error(403, "超级管理员账号不可删除");
        }
        Long currentId = currentAdminId();
        boolean isOwnCreated = currentId != null && currentId.equals(existing.getCreatedBy());
        if (!isOwnCreated) {
            return Result.error(403, "只能删除自己创建的账号");
        }
        repo.deleteById(id);
        return Result.success();
    }
}
