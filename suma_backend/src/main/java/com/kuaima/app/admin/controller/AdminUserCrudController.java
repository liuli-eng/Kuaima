package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

/** 管理员账号管理 */
@RestController
@RequestMapping("/admin/admin-users")
public class AdminUserCrudController {

    private final AdminUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AdminUserCrudController(AdminUserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Result<Page<AdminUser>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<AdminUser> result = repo.findAll(pageable);
        return Result.success(result, page, result.getTotalElements());
    }

    /** 管理员详情 */
    @GetMapping("/{id}")
    public Result<AdminUser> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @PostMapping
    public Result<AdminUser> create(@RequestBody AdminUser admin) {
        if (repo.existsByUsername(admin.getUsername())) {
            return Result.error(400, "账号已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(admin));
    }

    @PutMapping("/{id}")
    public Result<AdminUser> update(@PathVariable Long id, @RequestBody AdminUser patch) {
        AdminUser existing = repo.findById(id).orElseThrow();
        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getRole() != null) existing.setRole(patch.getRole());
        if (patch.getDept() != null) existing.setDept(patch.getDept());
        if (patch.getPhone() != null) existing.setPhone(patch.getPhone());
        if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        if (patch.getPassword() != null && !patch.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(patch.getPassword()));
        }
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    /** 重置密码 */
    @PutMapping("/{id}/reset-password")
    public Result<AdminUser> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        AdminUser existing = repo.findById(id).orElseThrow();
        String pwd = body.getOrDefault("newPassword", "admin123");
        existing.setPassword(passwordEncoder.encode(pwd));
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}
