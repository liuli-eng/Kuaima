package com.kuaima.app.admin.controller;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.admin.dto.AdminLoginDto;
import com.kuaima.app.admin.entity.AdminUser;
import com.kuaima.app.admin.repository.AdminUserRepository;
import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.security.util.JwtUtil;

import java.time.LocalDateTime;

/**
 * 后台管理员认证接口（账号密码登录）
 */
@RestController
@RequestMapping("/admin/auth")
@Tag(name = "后台-登录", description = "管理员登录鉴权")
public class AdminAuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminAuthController(AdminUserRepository adminUserRepository,
                               PasswordEncoder passwordEncoder,
                               JwtUtil jwtUtil) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "管理员登录", description = "账号密码登录，返回 accessToken（JWT role 前缀 ADMIN_）、adminId、username、name、role（SUPER_ADMIN/ADMIN/EDITOR/VIEWER）、permissions（权限树 JSON）；登录成功自动更新 lastLoginTime")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginDto dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            return Result.error(400, "用户名和密码不能为空");
        }
        AdminUser admin = adminUserRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            return Result.error(400, "用户名或密码错误");
        }
        if ("禁用".equals(admin.getStatus())) {
            return Result.error(403, "账号已被禁用");
        }
        admin.setLastLoginTime(LocalDateTime.now());
        adminUserRepository.save(admin);

        // 生成 JWT，role 前缀加 ADMIN_ 区分
        String token = jwtUtil.generateAccessToken(admin.getUsername(), "ADMIN_" + admin.getRole(), admin.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", token);
        data.put("adminId", admin.getId());
        data.put("username", admin.getUsername());
        data.put("name", admin.getName());
        data.put("role", admin.getRole());
        data.put("permissions", admin.getPermissions());
        return Result.success(data);
    }

    @Operation(summary = "获取当前管理员", description = "返回当前登录管理员信息（含角色、权限树 permissions、创建者 createdBy 等），供前端路由守卫/菜单过滤/按钮鉴权使用")
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof LoginUser lu)) {
            return Result.error(401, "未登录或登录已过期");
        }
        AdminUser admin = adminUserRepository.findById(lu.id()).orElse(null);
        if (admin == null) {
            return Result.error(404, "管理员账号不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", admin.getId());
        data.put("username", admin.getUsername());
        data.put("name", admin.getName());
        data.put("role", admin.getRole());
        data.put("permissions", admin.getPermissions());
        data.put("createdBy", admin.getCreatedBy());
        data.put("email", admin.getEmail());
        return Result.success(data);
    }
}
