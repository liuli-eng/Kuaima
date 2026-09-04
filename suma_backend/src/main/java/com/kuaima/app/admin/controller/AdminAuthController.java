package com.kuaima.app.admin.controller;

import java.util.HashMap;
import java.util.Map;

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
import com.kuaima.app.security.util.JwtUtil;

import java.time.LocalDateTime;

/**
 * 后台管理员认证接口（账号密码登录）
 */
@RestController
@RequestMapping("/admin/auth")
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
        return Result.success(data);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.success(new HashMap<>());
    }
}
